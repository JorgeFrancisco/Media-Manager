package br.com.jorgemelo.nimbusfilemanager.quality;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Stream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import br.com.jorgemelo.nimbusfilemanager.quality.dto.Counter;
import br.com.jorgemelo.nimbusfilemanager.quality.dto.CoverageSummary;
import br.com.jorgemelo.nimbusfilemanager.quality.dto.CoverageTarget;

public final class QualitySummary {

	private static final Path SUREFIRE_REPORTS = Path.of("target", "surefire-reports");

	private static final Path JACOCO_REPORT = Path.of("target", "site", "jacoco", "jacoco.xml");

	private static final int TOP_TARGETS_LIMIT = 15;

	private QualitySummary() {
	}

	public static void main(String[] args) {
		Locale.setDefault(Locale.US);

		try {
			TestSummary tests = readTestSummary();

			CoverageSummary coverage = readCoverageSummary();

			List<CoverageTarget> targets = readTopCoverageTargets();

			printSummary(tests, coverage, targets);
		} catch (Exception exception) {
			System.out.println();
			System.out.println("=========================================================");
			System.out.println("Nimbus File Manager - Quality Summary");
			System.out.println("=========================================================");
			System.out.println();
			System.out.println("Não foi possível gerar o resumo de qualidade.");
			System.out.println("Motivo: " + exception.getMessage());
			System.out.println();
			System.out.println("=========================================================");
		}
	}

	private static TestSummary readTestSummary() throws Exception {
		if (!Files.isDirectory(SUREFIRE_REPORTS)) {
			throw new IOException("Diretório do Surefire não encontrado: " + SUREFIRE_REPORTS);
		}

		TestSummary summary = new TestSummary();

		try (Stream<Path> reports = Files.list(SUREFIRE_REPORTS)) {
			for (Path report : reports.filter(QualitySummary::isSurefireXmlReport).toList()) {
				Document document = parseXml(report);

				Element root = document.getDocumentElement();

				summary.tests += longAttribute(root, "tests");
				summary.failures += longAttribute(root, "failures");
				summary.errors += longAttribute(root, "errors");
				summary.skipped += longAttribute(root, "skipped");
			}
		}

		return summary;
	}

	private static boolean isSurefireXmlReport(Path path) {
		String fileName = path.getFileName().toString();

		return fileName.startsWith("TEST-") && fileName.endsWith(".xml");
	}

	private static CoverageSummary readCoverageSummary() throws Exception {
		Document document = readJaCoCoDocument();

		Element report = document.getDocumentElement();

		return new CoverageSummary(findCoverage(report, "INSTRUCTION"), findCoverage(report, "BRANCH"),
				findCoverage(report, "LINE"), findCoverage(report, "METHOD"), findCoverage(report, "CLASS"));
	}

	private static List<CoverageTarget> readTopCoverageTargets() throws Exception {
		Document document = readJaCoCoDocument();

		NodeList classNodes = document.getElementsByTagName("class");

		List<CoverageTarget> targets = new ArrayList<>();

		for (int index = 0; index < classNodes.getLength(); index++) {
			Element classElement = (Element) classNodes.item(index);

			Counter instructionCounter = findCounter(classElement, "INSTRUCTION");

			if (instructionCounter == null || instructionCounter.missed() == 0) {
				continue;
			}

			String className = classElement.getAttribute("name").replace('/', '.');

			if (isIgnoredCoverageTarget(className)) {
				continue;
			}

			targets.add(new CoverageTarget(className, instructionCounter.missed(), instructionCounter.covered(),
					instructionCounter.percentage()));
		}

		return targets.stream()
				.sorted(Comparator.comparingLong(CoverageTarget::missed).reversed()
						.thenComparingDouble(CoverageTarget::percentage).thenComparing(CoverageTarget::className))
				.limit(TOP_TARGETS_LIMIT).toList();
	}

	private static Document readJaCoCoDocument() throws Exception {
		if (!Files.isRegularFile(JACOCO_REPORT)) {
			throw new IOException("Relatório XML do JaCoCo não encontrado: " + JACOCO_REPORT);
		}

		return parseXml(JACOCO_REPORT);
	}

	private static Document parseXml(Path path) throws Exception {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
		factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);

		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

		factory.setExpandEntityReferences(false);
		factory.setXIncludeAware(false);
		factory.setNamespaceAware(false);

		return factory.newDocumentBuilder().parse(path.toFile());
	}

	private static double findCoverage(Element parent, String counterType) {
		Counter counter = findCounter(parent, counterType);

		if (counter == null) {
			throw new IllegalStateException("Contador JaCoCo não encontrado: " + counterType);
		}

		return counter.percentage();
	}

	private static Counter findCounter(Element parent, String counterType) {
		NodeList children = parent.getChildNodes();

		for (int index = 0; index < children.getLength(); index++) {
			Node child = children.item(index);

			if (!(child instanceof Element element)) {
				continue;
			}

			if (!"counter".equals(element.getTagName())) {
				continue;
			}

			if (!counterType.equals(element.getAttribute("type"))) {
				continue;
			}

			return new Counter(longAttribute(element, "missed"), longAttribute(element, "covered"));
		}

		return null;
	}

	private static long longAttribute(Element element, String attributeName) {
		String value = element.getAttribute(attributeName);

		if (value == null || value.isBlank()) {
			return 0;
		}

		return Long.parseLong(value);
	}

	private static void printSummary(TestSummary tests, CoverageSummary coverage, List<CoverageTarget> targets) {
		System.out.println();
		System.out.println("=========================================================");
		System.out.println("Nimbus File Manager - Quality Summary");
		System.out.println("=========================================================");
		System.out.println();

		System.out.println("Tests");
		System.out.println("---------------------------------------------------------");
		System.out.printf("Tests:          %d%n", tests.tests);
		System.out.printf("Failures:       %d%n", tests.failures);
		System.out.printf("Errors:         %d%n", tests.errors);
		System.out.printf("Skipped:        %d%n", tests.skipped);
		System.out.println();

		System.out.println("JaCoCo");
		System.out.println("---------------------------------------------------------");
		System.out.printf("Instruction:    %.2f%%%n", coverage.instruction());
		System.out.printf("Branch:         %.2f%%%n", coverage.branch());
		System.out.printf("Line:           %.2f%%%n", coverage.line());
		System.out.printf("Method:         %.2f%%%n", coverage.method());
		System.out.printf("Class:          %.2f%%%n", coverage.clazz());
		System.out.println();

		System.out.println("Top 15 Coverage Targets");
		System.out.println("---------------------------------------------------------");

		if (targets.isEmpty()) {
			System.out.println("Nenhuma classe com instruções não cobertas.");
		} else {
			for (int index = 0; index < targets.size(); index++) {
				CoverageTarget target = targets.get(index);

				System.out.printf("%d. %-42s %6d missed | %6.2f%%%n", index + 1,
						shortenClassName(target.className(), 42), target.missed(), target.percentage());
			}
		}

		System.out.println();
		System.out.println("=========================================================");
	}

	private static boolean isIgnoredCoverageTarget(String className) {
		return className.contains(".config.") || className.contains(".dto.") || className.contains(".repository.")
				|| className.endsWith("Application") || className.endsWith("Configuration")
				|| className.endsWith("Properties") || className.endsWith("Mapper") || className.contains("$");
	}

	private static String shortenClassName(String className, int maximumLength) {
		if (className.length() <= maximumLength) {
			return className;
		}

		String simpleName = className.substring(className.lastIndexOf('.') + 1);

		if (simpleName.length() <= maximumLength) {
			return simpleName;
		}

		return simpleName.substring(0, maximumLength - 3) + "...";
	}
}