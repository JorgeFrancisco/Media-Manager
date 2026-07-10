package br.com.jorgemelo.nimbusfilemanager.duplicate.application.dto;

import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.enums.Reason;
import br.com.jorgemelo.nimbusfilemanager.duplicate.domain.enums.Verdict;

public record Decision(Verdict verdict, Reason reason) {
}