(function () {
	var root = document.getElementById("mapRoot");

	if (!root || typeof L === "undefined") {
		return;
	}

	var i18n = window.NimbusFileManagerI18n, t = i18n.t;
	var lightbox = window.NimbusFileManagerLightbox;
	var dateFormatter = new Intl.DateTimeFormat(i18n.locale, { dateStyle: "medium" });

	var statusEl = document.getElementById("mapStatus");
	var panel = document.getElementById("mapPanel");
	var panelTitle = document.getElementById("mapPanelTitle");
	var panelCount = document.getElementById("mapPanelCount");
	var panelList = document.getElementById("mapPanelList");
	var panelMore = document.getElementById("mapPanelMore");
	var panelClose = document.getElementById("mapPanelClose");

	// EXIF pins sit at the real GPS point; administrative pins are approximate, so they get a
	// distinct colour (see the legend). Circle markers avoid the webjar default-icon image path.
	var COLORS = { EXIF: "#2563eb", ADMINISTRATIVE: "#ea580c" };

	// The thumbnail cache only holds 320px/640px; 320 is already warm across the app, so we reuse it
	// (displayed downscaled) instead of generating a new size.
	var THUMB_WIDTH = 320;
	// Density rule: with few pins in view show a representative thumbnail per location; when the view
	// gets crowded fall back to plain count pins so the DOM stays light.
	var DENSITY_LIMIT = 50;
	var POPUP_GRID = 12;
	var PANEL_PAGE = 50;
	// Only the pins in the viewport are loaded, capped for safety; the box is padded so small pans
	// reuse the loaded set instead of refetching on every move.
	var PIN_LIMIT = 2000;
	var BBOX_PAD = 0.3;

	var maxZoom = parseInt(root.dataset.maxZoom, 10) || 19;

	var map = L.map(root, { worldCopyJump: true, preferCanvas: true }).setView([0, 0], 2);
	L.tileLayer(root.dataset.tileUrl, { maxZoom: maxZoom, attribution: root.dataset.tileAttribution || "" }).addTo(map);

	var pins = [];
	var circles = [];
	var thumbMarkers = {};
	var loadedBounds = null, loadedZoom = null, pinsController = null, firstLoad = true;
	var panelPin = null, panelPage = 0, panelLoading = false, panelController = null;

	requestPins();

	map.on("moveend zoomend", debounce(maybeRefresh, 300));

	// Reload the pins only when the view leaves the loaded (padded) area or the zoom changed;
	// panning inside the loaded box just re-evaluates density on the pins already in memory.
	function maybeRefresh() {
		if (!loadedBounds || map.getZoom() !== loadedZoom || !loadedBounds.contains(map.getBounds())) {
			requestPins();
		} else {
			updateDensity();
		}
	}

	function requestPins() {
		var padded = map.getBounds().pad(BBOX_PAD);
		var sw = padded.getSouthWest(), ne = padded.getNorthEast();

		if (pinsController) {
			pinsController.abort();
		}

		pinsController = new AbortController();

		if (firstLoad) {
			statusEl.textContent = t("js.map.loading");
		}

		fetch("/api/map/pins?minLat=" + sw.lat + "&minLon=" + sw.lng + "&maxLat=" + ne.lat + "&maxLon=" + ne.lng
				+ "&limit=" + PIN_LIMIT + "&zoom=" + map.getZoom(), { signal: pinsController.signal })
			.then(function (response) { return response.json(); })
			.then(function (data) { setPins(data || [], padded); })
			.catch(function (error) {
				if (error.name !== "AbortError") {
					statusEl.textContent = t("js.map.error");
				}
			});
	}

	function setPins(data, padded) {
		clearMarkers();

		pins = data;
		loadedBounds = padded;
		loadedZoom = map.getZoom();

		var fit = [];

		pins.forEach(function (pin, index) {
			var circle = L.circleMarker([pin.latitude, pin.longitude], {
				radius: 7, weight: 2, color: "#ffffff",
				fillColor: COLORS[pin.source] || COLORS.EXIF, fillOpacity: 0.9
			});

			circle.bindTooltip(escapeHtml(pin.label) + " · " + pin.total);
			circle.on("click", clickHandler(index));
			circle.addTo(map);

			circles[index] = circle;
			fit.push([pin.latitude, pin.longitude]);
		});

		if (firstLoad && fit.length) {
			map.fitBounds(fit, { padding: [40, 40], maxZoom: 12 });
		}

		firstLoad = false;
		statusEl.textContent = pins.length ? (pins.length >= PIN_LIMIT ? t("js.map.zoomIn") : "") : t("js.map.empty");

		updateDensity();
	}

	function clearMarkers() {
		circles.forEach(function (circle) { if (circle) { map.removeLayer(circle); } });
		Object.keys(thumbMarkers).forEach(function (key) { map.removeLayer(thumbMarkers[key]); });

		circles = [];
		thumbMarkers = {};
	}

	// Promote the pins currently in view to thumbnail markers when the view is sparse, and demote
	// them back to count pins when it gets crowded - touching only the handful that change.
	function updateDensity() {
		if (!pins.length) {
			return;
		}

		var view = map.getBounds();
		var visible = [];

		for (var i = 0; i < pins.length; i++) {
			if (view.contains([pins[i].latitude, pins[i].longitude])) {
				visible.push(i);
			}
		}

		var desired = {};

		if (visible.length && visible.length <= DENSITY_LIMIT) {
			visible.forEach(function (index) {
				if (pins[index].coverMediaId) {
					desired[index] = true;
				}
			});
		}

		Object.keys(thumbMarkers).forEach(function (key) {
			if (!desired[key]) {
				map.removeLayer(thumbMarkers[key]);
				delete thumbMarkers[key];

				var index = parseInt(key, 10);

				if (!map.hasLayer(circles[index])) {
					circles[index].addTo(map);
				}
			}
		});

		Object.keys(desired).forEach(function (key) {
			if (!thumbMarkers[key]) {
				var index = parseInt(key, 10);

				if (map.hasLayer(circles[index])) {
					map.removeLayer(circles[index]);
				}

				var marker = L.marker([pins[index].latitude, pins[index].longitude], {
					icon: thumbIcon(pins[index]), keyboard: true, title: pins[index].label
				});

				marker.on("click", clickHandler(index));
				marker.addTo(map);

				thumbMarkers[key] = marker;
			}
		});
	}

	function thumbIcon(pin) {
		var frame = pin.source === "ADMINISTRATIVE" ? "map-thumb map-thumb-admin" : "map-thumb map-thumb-exif";
		var count = pin.total > 1 ? '<b class="map-thumb-count">' + pin.total + "</b>" : "";
		var src = "/api/media/" + pin.coverMediaId + "/thumbnail?w=" + THUMB_WIDTH;

		return L.divIcon({
			className: "map-thumb-icon",
			html: '<span class="' + frame + '"><img src="' + src + '" loading="lazy" decoding="async" alt="">'
					+ count + "</span>",
			iconSize: [58, 58], iconAnchor: [29, 29], popupAnchor: [0, -30]
		});
	}

	function clickHandler(index) {
		return function () { activatePin(index); };
	}

	function activatePin(index) {
		var pin = pins[index];

		if (pin.total === 1 && pin.coverMediaId) {
			openInLightbox(mediaLink(pin.coverMediaId, pin.coverFileType, pin.coverFileName));
		} else {
			openPopup(index);
		}
	}

	// A pin with several media opens a small grid in a Leaflet popup; each cell is a shared lightbox
	// trigger (js-lightbox-*), so clicking reuses NimbusFileManagerLightbox and its prev/next navigation.
	function openPopup(index) {
		var pin = pins[index];
		var marker = thumbMarkers[index] || circles[index];

		var container = document.createElement("div");
		container.className = "map-popup";

		var title = document.createElement("strong");
		title.className = "map-popup-title";
		title.textContent = pin.label;

		var count = document.createElement("span");
		count.className = "muted map-popup-count";
		count.innerHTML = countHtml(pin);

		var grid = document.createElement("div");
		grid.className = "map-popup-grid";
		grid.textContent = t("js.map.loading");

		container.appendChild(title);
		container.appendChild(count);
		container.appendChild(grid);

		marker.unbindPopup();
		marker.bindPopup(container, { minWidth: 236, maxWidth: 260, className: "map-popup-wrap" }).openPopup();

		fetch("/api/map/items?pinId=" + encodeURIComponent(pin.pinId) + "&page=0&size=" + POPUP_GRID)
			.then(function (response) { return response.json(); })
			.then(function (page) {
				var items = page.content || [];

				grid.textContent = "";
				items.forEach(function (item) { grid.appendChild(mediaTile(item)); });

				if (pin.total > items.length) {
					var all = document.createElement("button");
					all.type = "button";
					all.className = "button-secondary map-popup-all";
					all.textContent = t("js.map.viewAll") + " (" + pin.total + ")";
					all.addEventListener("click", function () { marker.closePopup(); openPanel(index); });
					container.appendChild(all);
				}

				marker.getPopup().update();
			}).catch(function () { grid.textContent = t("js.map.itemsError"); });
	}

	// The sidebar only exists for the extra job the popup grid can't do: paginating a large location.
	function openPanel(index) {
		panelPin = pins[index];
		panelPage = 0;

		if (panelController) {
			panelController.abort();
		}

		panelTitle.textContent = panelPin.label;
		panelCount.innerHTML = countHtml(panelPin);
		panelList.textContent = "";
		panel.classList.add("is-open");

		loadPanel();
	}

	function loadPanel() {
		if (panelLoading || !panelPin) {
			return;
		}

		panelLoading = true;
		panelMore.hidden = true;
		panelController = new AbortController();

		fetch("/api/map/items?pinId=" + encodeURIComponent(panelPin.pinId) + "&page=" + panelPage + "&size="
				+ PANEL_PAGE, { signal: panelController.signal })
			.then(function (response) { return response.json(); })
			.then(function (page) {
				(page.content || []).forEach(function (item) { panelList.appendChild(panelRow(item)); });

				panelPage++;
				panelMore.hidden = page.last !== false;
				panelLoading = false;
			}).catch(function (error) {
				if (error.name === "AbortError") {
					return;
				}

				panelLoading = false;
				panelMore.hidden = true;
				appendError(panelList);
			});
	}

	function mediaTile(item) {
		var tile = triggerLink(item, "map-tile");

		var image = document.createElement("img");
		image.className = "map-tile-thumb";
		image.loading = "lazy";
		image.decoding = "async";
		image.alt = "";
		image.src = "/api/media/" + item.mediaId + "/thumbnail?w=" + THUMB_WIDTH;
		image.addEventListener("error", function () { tile.classList.add("thumbnail-error"); });

		tile.appendChild(image);

		if (item.fileType === "VIDEO") {
			var badge = document.createElement("span");
			badge.className = "map-tile-badge";
			badge.innerHTML = '<i class="bi bi-play-fill"></i>';
			tile.appendChild(badge);
		}

		return tile;
	}

	function panelRow(item) {
		var link = triggerLink(item, "map-panel-link");

		var thumb = document.createElement("img");
		thumb.className = "map-panel-thumb";
		thumb.loading = "lazy";
		thumb.decoding = "async";
		thumb.alt = "";
		thumb.src = "/api/media/" + item.mediaId + "/thumbnail?w=" + THUMB_WIDTH;
		thumb.addEventListener("error", function () { link.classList.add("thumbnail-error"); });

		var name = document.createElement("span");
		name.className = "map-panel-name";
		name.textContent = item.fileName;

		var date = document.createElement("span");
		date.className = "muted map-panel-date";
		date.textContent = item.captureDate ? dateFormatter.format(new Date(item.captureDate)) : "";

		var meta = document.createElement("div");
		meta.className = "map-panel-meta";
		meta.appendChild(name);
		meta.appendChild(date);

		link.appendChild(thumb);
		link.appendChild(meta);

		var li = document.createElement("li");
		li.className = "map-panel-item";
		li.appendChild(link);

		return li;
	}

	// Builds the shared lightbox trigger. Leaflet may swallow the click before it reaches the global
	// delegated listener, so we call openInventoried explicitly; the element still lives in the DOM,
	// so the lightbox's prev/next (which scans js-lightbox-* nodes) sees its siblings.
	function triggerLink(item, extraClass) {
		return decorateLink(mediaLink(item.mediaId, item.fileType, item.fileName), extraClass);
	}

	function mediaLink(mediaId, fileType, name) {
		var link = document.createElement("a");
		link.href = "javascript:void(0)";
		link.className = fileType === "VIDEO" ? "js-lightbox-video" : "js-lightbox-image";
		link.dataset.publicId = mediaId;
		link.dataset.mediaType = fileType;

		if (name) {
			link.dataset.name = name;
		}

		return link;
	}

	function decorateLink(link, extraClass) {
		link.className += " " + extraClass;
		link.addEventListener("click", function (event) {
			event.preventDefault();
			openInLightbox(link);
		});

		return link;
	}

	function openInLightbox(link) {
		if (lightbox && lightbox.openInventoried) {
			lightbox.openInventoried(link);
		}
	}

	function countHtml(pin) {
		return pin.total + ' · <i class="bi bi-image"></i> ' + pin.photos + ' · <i class="bi bi-camera-video"></i> '
				+ pin.videos;
	}

	function appendError(list) {
		var li = document.createElement("li");
		li.className = "muted map-panel-error";
		li.textContent = t("js.map.itemsError");
		list.appendChild(li);
	}

	function escapeHtml(value) {
		var span = document.createElement("span");
		span.textContent = value == null ? "" : value;

		return span.innerHTML;
	}

	function debounce(fn, wait) {
		var timer;

		return function () {
			clearTimeout(timer);
			timer = setTimeout(fn, wait);
		};
	}

	panelMore.addEventListener("click", loadPanel);

	panelClose.addEventListener("click", function () {
		panel.classList.remove("is-open");
		panelPin = null;

		if (panelController) {
			panelController.abort();
		}
	});
})();