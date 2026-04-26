const locationForm = document.getElementById("location-form");
const locationsTableBody = document.getElementById("locations-table-body");
const messageBox = document.getElementById("message");
const reloadAllBtn = document.getElementById("reload-all-btn");
const searchContainer = document.getElementById("location-search-container");
const editingIdInput = document.getElementById("editing-location-id");
const submitBtn = document.getElementById("submit-btn");
const cancelEditBtn = document.getElementById("cancel-edit-btn");
const loginLink = document.getElementById("login-link");
const logoutForm = document.getElementById("logout-form");

let locationsState = [];
let locationSearchRequestSeq = 0;

let authState = {
    authenticated: false,
    username: null,
    roles: []
};

/* ----------------------------- App bootstrap ----------------------------- */

document.addEventListener("DOMContentLoaded", async () => {
    await loadAuthState();
    updateUiByAuth();
    renderLocationSearch();
    await loadLocations();
});

/* ------------------------------ UI events ------------------------------- */

reloadAllBtn.addEventListener("click", async () => {
    try {
        setFormBusy(true);
        await loadLocations();
        showMessage(t("messages.weatherDataRefreshed"), false);
    } catch (error) {
        showMessage(error.message, true);
    } finally {
        setFormBusy(false);
    }
});

locationForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const editingId = editingIdInput.value;
    const payload = serializeLocationFormValues();

    const isEdit = Boolean(editingId);
    const url = isEdit ? `/api/locations/${editingId}` : "/api/locations";
    const method = isEdit ? "PUT" : "POST";

    try {
        setFormBusy(true);

        const response = await fetch(url, {
            method,
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const errorMessage = await tryReadError(response);
            showMessage(errorMessage, true);
            return;
        }

        resetFormMode();
        showMessage(
            isEdit ? t("messages.locationUpdated") : t("messages.locationAdded"),
            false
        );
        await loadLocations();
    } catch (error) {
        showMessage(error.message, true);
    } finally {
        setFormBusy(false);
    }
});

cancelEditBtn.addEventListener("click", () => {
    resetFormMode();
});

/* ---------------------------- Auth handling ----------------------------- */

async function loadAuthState() {
    try {
        const response = await fetch("/api/auth/me");

        if (!response.ok) {
            authState = {
                authenticated: false,
                username: null,
                roles: []
            };
            return;
        }

        authState = await response.json();
    } catch {
        authState = {
            authenticated: false,
            username: null,
            roles: []
        };
    }
}

function canManageLocations() {
    return authState.authenticated && authState.roles.includes("ROLE_ADMIN");
}

function updateUiByAuth() {
    const formPanel = document.getElementById("location-form-panel");

    if (formPanel) {
        formPanel.style.display = canManageLocations() ? "block" : "none";
    }

    if (loginLink) {
        loginLink.style.display = authState.authenticated ? "none" : "inline";
    }

    if (logoutForm) {
        logoutForm.style.display = authState.authenticated ? "inline-block" : "none";
    }
}

/* -------------------------- Locations data loading ---------------------- */

async function loadLocations() {
    clearMessage();

    try {
        const response = await fetch("/api/locations");

        if (!response.ok) {
            const errorMessage = await tryReadError(response);
            showMessage(errorMessage, true);
            return;
        }

        const rawLocations = await response.json();

        locationsState = rawLocations
            .map((rawLocation, index) => {
                const location = normalizeLocation(rawLocation);
                return {
                    ...location,
                    sortOrder: location.sortOrder ?? index + 1
                };
            })
            .sort((a, b) => a.sortOrder - b.sortOrder);

        await renderLocations();
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function renderLocations() {
    locationsTableBody.innerHTML = "";

    for (const location of locationsState) {
        const weather = await loadWeather(location.id);
        const row = buildLocationRow(location, weather);
        locationsTableBody.appendChild(row);
    }
}

async function loadWeather(locationId) {
    try {
        const response = await fetch(`/api/weather?locationId=${locationId}`);

        if (!response.ok) {
            return null;
        }

        return await response.json();
    } catch {
        return null;
    }
}

/* --------------------------- Location actions --------------------------- */

function buildLocationRow(location, weather) {
    const tr = document.createElement("tr");
    tr.className = "location-row";

    tr.innerHTML = `
        <td class="col-location-cell" data-label="${escapeHtml(t("labels.location"))}">
            <div class="cell-value location-value">
                ${buildLocationLabel(location)}
            </div>
        </td>
        <td class="col-num-cell" data-label="${escapeHtml(t("labels.temperatureShort"))}">
            <span class="cell-value">${formatNumber(weather?.temperature, 1)}</span>
        </td>
        <td class="col-num-cell" data-label="${escapeHtml(t("labels.feelsLikeShort"))}">
            <span class="cell-value">${formatNumber(weather?.feelsLike, 1)}</span>
        </td>
        <td class="col-num-cell" data-label="${escapeHtml(t("labels.humidity"))}">
            <span class="cell-value">${formatNumber(weather?.humidity, 0)}</span>
        </td>
        <td class="col-num-cell" data-label="${escapeHtml(t("labels.pressure"))}">
            <span class="cell-value">${formatNumber(weather?.pressure, 0)}</span>
        </td>
        <td class="col-num-cell" data-label="${escapeHtml(t("labels.windSpeed"))}">
            <span class="cell-value">${formatWind(weather?.windSpeed)}</span>
        </td>
        <td class="col-num-cell" data-label="${escapeHtml(t("labels.clouds"))}">
            <span class="cell-value">${formatNumber(weather?.cloudsAll ?? weather?.cloudsPercentage, 0)}</span>
        </td>
        <td class="col-desc-cell" data-label="${escapeHtml(t("labels.descriptionShort"))}">
            <span class="cell-value desc-value">${escapeHtml(weather?.description ?? "-")}</span>
        </td>
        <td class="col-actions-cell" data-label="${escapeHtml(t("labels.actions"))}">
            <div class="cell-value actions-value">
                ${buildActionsHtml()}
            </div>
        </td>
    `;

    tr.querySelector(".forecast-btn")?.addEventListener("click", async () => {
        await toggleForecastRow(tr, location.id);
    });

    tr.querySelector(".edit-btn")?.addEventListener("click", () => {
        startEdit(location);
    });

    tr.querySelector(".delete-btn")?.addEventListener("click", async () => {
        const confirmed = window.confirm(
            t("confirmations.deleteLocation", {
                name: location.name || t("labels.location").toLowerCase()
            })
        );

        if (!confirmed) {
            return;
        }

        try {
            const response = await fetch(`/api/locations/${location.id}`, {
                method: "DELETE"
            });

            if (!response.ok) {
                const errorMessage = await tryReadError(response);
                showMessage(errorMessage, true);
                return;
            }

            showMessage(t("messages.locationDeleted"), false);
            await loadLocations();
        } catch (error) {
            showMessage(error.message, true);
        }
    });

    tr.querySelector(".move-up-btn")?.addEventListener("click", async () => {
        await moveLocation(location.id, -1);
    });

    tr.querySelector(".move-down-btn")?.addEventListener("click", async () => {
        await moveLocation(location.id, 1);
    });

    return tr;
}

function serializeLocationFormValues() {
    return {
        name: document.getElementById("name").value.trim(),
        country: document.getElementById("country").value.trim(),
        region: document.getElementById("region").value.trim() || null,
        longitude: parseFloat(document.getElementById("longitude").value),
        latitude: parseFloat(document.getElementById("latitude").value)
    };
}

function startEdit(location) {
    resetLocationSearch(true);

    editingIdInput.value = location.id;
    document.getElementById("name").value = location.name ?? "";
    document.getElementById("country").value = location.country ?? "";
    document.getElementById("region").value = location.region ?? "";
    document.getElementById("longitude").value = location.longitude ?? "";
    document.getElementById("latitude").value = location.latitude ?? "";

    submitBtn.textContent = t("buttons.saveChanges");
    cancelEditBtn.hidden = false;

    locationForm.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}

function resetFormMode() {
    editingIdInput.value = "";
    locationForm.reset();
    submitBtn.textContent = t("buttons.addLocation");
    cancelEditBtn.hidden = true;
}

async function moveLocation(locationId, direction) {
    const currentIndex = locationsState.findIndex((location) => location.id === locationId);

    if (currentIndex === -1) {
        return;
    }

    const targetIndex = currentIndex + direction;

    if (targetIndex < 0 || targetIndex >= locationsState.length) {
        return;
    }

    [locationsState[currentIndex], locationsState[targetIndex]] =
        [locationsState[targetIndex], locationsState[currentIndex]];

    locationsState = locationsState.map((location, index) => ({
        ...location,
        sortOrder: index + 1
    }));

    await renderLocations();

    try {
        await persistSortOrder();
        showMessage(t("messages.displayOrderUpdated"), false);
    } catch (error) {
        showMessage(error.message, true);
        await loadLocations();
    }
}

function buildActionsHtml() {
    const moveUpTitle = escapeHtml(t("buttons.moveUp"));
    const moveDownTitle = escapeHtml(t("buttons.moveDown"));

    if (!canManageLocations()) {
        return `
            <div class="actions">
                <button type="button" class="btn btn-primary btn-md btn-action action-btn forecast-btn">
                    ${escapeHtml(t("buttons.forecast"))}
                </button>
            </div>
        `;
    }

    return `
        <div class="actions">
            <div class="order-actions">
                <button
                    type="button"
                    class="btn btn-secondary btn-icon order-btn move-up-btn"
                    title="${moveUpTitle}"
                >
                    ↑
                </button>
                <button
                    type="button"
                    class="btn btn-secondary btn-icon order-btn move-down-btn"
                    title="${moveDownTitle}"
                >
                    ↓
                </button>
            </div>

            <button type="button" class="btn btn-primary btn-md btn-action action-btn forecast-btn">
                ${escapeHtml(t("buttons.forecast"))}
            </button>
            <button type="button" class="btn btn-warning btn-md btn-action action-btn edit-btn">
                ${escapeHtml(t("buttons.edit"))}
            </button>
            <button type="button" class="btn btn-danger btn-md btn-action action-btn delete-btn">
                ${escapeHtml(t("buttons.delete"))}
            </button>
        </div>
    `;
}

async function persistSortOrder() {
    const payload = locationsState.map((location, index) => ({
        locationId: location.id,
        sortOrder: index + 1
    }));

    const response = await fetch("/api/locations/order", {
        method: "PUT",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify(payload)
    });

    if (!response.ok) {
        const error = await tryReadError(response);
        throw new Error(error);
    }

    const updatedRawLocations = await response.json();

    locationsState = updatedRawLocations
        .map((rawLocation, index) => {
            const location = normalizeLocation(rawLocation);
            return {
                ...location,
                sortOrder: location.sortOrder ?? index + 1
            };
        })
        .sort((a, b) => a.sortOrder - b.sortOrder);
}

/* --------------------------- Location search ---------------------------- */

function debounce(fn, delay = 300) {
    let timeoutId;

    return (...args) => {
        clearTimeout(timeoutId);
        timeoutId = setTimeout(() => fn(...args), delay);
    };
}

const debouncedLocationSearch = debounce(() => {
    performLocationSearch();
}, 300);

function renderLocationSearch() {
    if (!canManageLocations()) {
        return;
    }

    searchContainer.innerHTML = `
        <div class="location-search-panel">
            <div class="location-search-header">
                <p class="location-search-hint">${escapeHtml(t("hints.locationSearch"))}</p>
            </div>

            <div class="location-search-box">
                <input
                    type="text"
                    id="location-search-input"
                    placeholder="${escapeHtml(t("placeholders.locationSearch"))}"
                >

                <div class="location-search-actions">
                    <button type="button" id="location-search-btn" class="btn btn-primary btn-md">
                        ${escapeHtml(t("buttons.search"))}
                    </button>
                    <button
                        type="button"
                        id="location-search-cancel-btn"
                        class="btn btn-secondary btn-md"
                        hidden
                    >
                        ${escapeHtml(t("buttons.cancel"))}
                    </button>
                </div>
            </div>

            <div id="location-search-results" class="location-search-results" hidden></div>
        </div>
    `;

    initLocationSearchEvents();
}

function initLocationSearchEvents() {
    const input = document.getElementById("location-search-input");
    const button = document.getElementById("location-search-btn");
    const cancelBtn = document.getElementById("location-search-cancel-btn");

    if (!input || !button || !cancelBtn) {
        return;
    }

    button.addEventListener("click", async () => {
        await performLocationSearch();
    });

    cancelBtn.addEventListener("click", () => {
        resetLocationSearch(true);
        input.blur();
    });

    input.addEventListener("keydown", async (event) => {
        if (event.key === "Enter") {
            event.preventDefault();
            await performLocationSearch();
        }

        if (event.key === "Escape") {
            resetLocationSearch(true);
            input.blur();
        }
    });

    input.addEventListener("input", () => {
        if (!input.value.trim()) {
            resetLocationSearch(false);
            return;
        }

        debouncedLocationSearch();
    });
}

async function performLocationSearch() {
    const input = document.getElementById("location-search-input");

    if (!input) {
        return;
    }

    const query = input.value.trim();
    const cancelBtn = document.getElementById("location-search-cancel-btn");

    if (!query) {
        resetLocationSearch(false);
        return;
    }

    const requestId = ++locationSearchRequestSeq;

    try {
        setSearchBusy(true);

        const response = await fetch(`/api/locations/search?q=${encodeURIComponent(query)}`);

        if (!response.ok) {
            const error = await tryReadError(response);
            throw new Error(error);
        }

        const rawResults = await response.json();
        const results = rawResults.map(normalizeLocationSearchResult);

        if (requestId !== locationSearchRequestSeq) {
            return;
        }

        if (input.value.trim() !== query) {
            return;
        }

        renderSearchResults(results);

        if (cancelBtn) {
            cancelBtn.hidden = false;
        }
    } catch (error) {
        showMessage(error.message, true);
    } finally {
        if (requestId === locationSearchRequestSeq) {
            setSearchBusy(false);
        }
    }
}

function renderSearchResults(results) {
    const container = document.getElementById("location-search-results");

    if (!container) {
        return;
    }

    container.hidden = false;

    if (!results || results.length === 0) {
        container.innerHTML = `<div class="location-search-empty">${escapeHtml(t("errors.noSearchResults"))}</div>`;
        return;
    }

    container.innerHTML = results
        .map((result) => `
            <div
                class="location-result-item"
                data-name="${escapeHtml(result.name)}"
                data-region="${escapeHtml(result.region ?? "")}"
                data-country="${escapeHtml(result.country)}"
                data-lat="${escapeHtml(String(result.latitude))}"
                data-lon="${escapeHtml(String(result.longitude))}"
            >
                <div class="location-result-label">${escapeHtml(result.label)}</div>
                <div class="location-result-coords">
                    lat: ${result.latitude.toFixed(4)}, lon: ${result.longitude.toFixed(4)}
                </div>
            </div>
        `)
        .join("");

    attachResultClickHandlers();
}

function attachResultClickHandlers() {
    const results = document.querySelectorAll(".location-result-item");

    results.forEach((element) => {
        element.addEventListener("click", () => {
            results.forEach((item) => item.classList.remove("selected"));
            element.classList.add("selected");

            editingIdInput.value = "";
            document.getElementById("name").value = element.dataset.name;
            document.getElementById("region").value = element.dataset.region;
            document.getElementById("country").value = element.dataset.country;
            document.getElementById("latitude").value = element.dataset.lat;
            document.getElementById("longitude").value = element.dataset.lon;

            submitBtn.textContent = t("buttons.addLocation");
            cancelEditBtn.hidden = false;

            showMessage(t("messages.formFilledFromSearch"), false);

            const resultsContainer = document.getElementById("location-search-results");
            if (resultsContainer) {
                resultsContainer.hidden = true;
            }

            locationForm.scrollIntoView({
                behavior: "smooth",
                block: "start"
            });
        });
    });
}

function resetLocationSearch(clearInput = true) {
    const input = document.getElementById("location-search-input");
    const resultsContainer = document.getElementById("location-search-results");
    const cancelBtn = document.getElementById("location-search-cancel-btn");

    if (input && clearInput) {
        input.value = "";
    }

    if (resultsContainer) {
        resultsContainer.hidden = true;
        resultsContainer.innerHTML = "";
    }

    if (cancelBtn) {
        cancelBtn.hidden = true;
    }
}

/* ----------------------------- Forecast UI ------------------------------ */

async function toggleForecastRow(locationRow, locationId) {
    const nextRow = locationRow.nextElementSibling;

    if (nextRow && nextRow.classList.contains("forecast-details-row")) {
        nextRow.remove();
        return;
    }

    const detailsRow = document.createElement("tr");
    detailsRow.className = "forecast-details-row";
    detailsRow.innerHTML = `
        <td colspan="9">
            <div class="forecast-loading">${escapeHtml(t("weather.loadingForecast"))}</div>
        </td>
    `;

    locationRow.insertAdjacentElement("afterend", detailsRow);

    try {
        const forecast = await loadForecast(locationId);
        detailsRow.innerHTML = `
            <td colspan="9">
                ${buildForecastTable(forecast)}
            </td>
        `;
    } catch (error) {
        detailsRow.innerHTML = `
            <td colspan="9">
                <div class="message-error">${escapeHtml(t("errors.forecastLoadFailed"))}</div>
            </td>
        `;
    }
}

async function loadForecast(locationId) {
    const response = await fetch(`/api/weather/forecast?locationId=${locationId}`);

    if (!response.ok) {
        const error = await tryReadError(response);
        throw new Error(error);
    }

    return await response.json();
}

function buildForecastTable(items) {
    if (!items || items.length === 0) {
        return `<div class="forecast-empty">${escapeHtml(t("errors.noForecastData"))}</div>`;
    }

    const grouped = groupForecastByDay(items);
    let dayIndex = 0;

    const rows = grouped
        .map((group) => {
            const dayClass = dayIndex % 2 === 0 ? "forecast-day-even" : "forecast-day-odd";
            dayIndex++;

            return group.items
                .map((item, index) => {
                    const dayCell = index === 0
                        ? `
                            <td class="forecast-day-cell ${dayClass}" rowspan="${group.items.length}">
                                <div class="forecast-day-name">${escapeHtml(group.dayName)}</div>
                                <div class="forecast-day-label">${escapeHtml(group.relativeLabel)}</div>
                                <div class="forecast-day-date">(${escapeHtml(group.shortDate)})</div>
                            </td>
                        `
                        : "";

                    return `
                        <tr class="${dayClass}">
                            ${dayCell}
                            <td>${formatHour(item.dateTime)}</td>
                            <td class="${getTemperatureClass(item.temperature)}">${formatNumber(item.temperature, 1)}</td>
                            <td class="${getTemperatureClass(item.feelsLike)}">${formatNumber(item.feelsLike, 1)}</td>
                            <td>${formatWind(item.windSpeed)}</td>
                            <td class="${getRainClass(item.rainVolume)}">${formatNumber(item.rainVolume, 1)}</td>
                            <td class="${getSnowClass(item.snowVolume)}">${formatNumber(item.snowVolume, 1)}</td>
                            <td class="${getPrecipitationClass(item.precipitationProbability)}">${formatPercentValue(item.precipitationProbability)}</td>
                            <td>${escapeHtml(item.description ?? "-")}</td>
                        </tr>
                    `;
                })
                .join("");
        })
        .join("");

    return `
        <div class="forecast-wrapper">
            <table class="forecast-table">
                <thead>
                    <tr>
                        <th>
                            <div class="th-stack">
                                <span class="th-title">${escapeHtml(t("labels.day"))}</span>
                            </div>
                        </th>
                        <th>
                            <div class="th-stack">
                                <span class="th-title">${escapeHtml(t("labels.hour"))}</span>
                            </div>
                        </th>
                        <th>
                            <div class="th-stack">
                                <span class="th-title">${escapeHtml(t("labels.temperatureShort"))}</span>
                                <span class="th-unit">${escapeHtml(t("units.temperature"))}</span>
                            </div>
                        </th>
                        <th>
                            <div class="th-stack">
                                <span class="th-title">${escapeHtml(t("labels.feelsLikeShort"))}</span>
                                <span class="th-unit">${escapeHtml(t("units.temperature"))}</span>
                            </div>
                        </th>
                        <th>
                            <div class="th-stack">
                                <span class="th-title">${escapeHtml(t("labels.wind"))}</span>
                                <span class="th-unit">${escapeHtml(t("units.windSpeed"))}</span>
                            </div>
                        </th>
                        <th>
                            <div class="th-stack">
                                <span class="th-title">${escapeHtml(t("labels.rain"))}</span>
                                <span class="th-unit">${escapeHtml(t("units.rain"))}</span>
                            </div>
                        </th>
                        <th>
                            <div class="th-stack">
                                <span class="th-title">${escapeHtml(t("labels.snow"))}</span>
                                <span class="th-unit">${escapeHtml(t("units.snow"))}</span>
                            </div>
                        </th>
                        <th>
                            <div class="th-stack">
                                <span class="th-title">${escapeHtml(t("labels.precipitationChance"))}</span>
                                <span class="th-unit">${escapeHtml(t("units.precipitationChance"))}</span>
                            </div>
                        </th>
                        <th>
                            <div class="th-stack">
                                <span class="th-title">${escapeHtml(t("labels.description"))}</span>
                            </div>
                        </th>
                    </tr>
                </thead>
                <tbody>${rows}</tbody>
            </table>
        </div>
    `;
}

function groupForecastByDay(items) {
    const groups = new Map();

    for (const item of items) {
        const date = new Date(item.dateTime);
        const key = getDateKey(date);

        if (!groups.has(key)) {
            groups.set(key, {
                key,
                date,
                items: []
            });
        }

        groups.get(key).items.push(item);
    }

    return Array.from(groups.values()).map((group) => ({
        ...group,
        dayName: formatDayName(group.date),
        relativeLabel: formatRelativeDayLabel(group.date),
        shortDate: formatShortDate(group.date)
    }));
}

/* ---------------------------- Format helpers ---------------------------- */

function normalizeLocation(rawLocation) {
    return {
        ...rawLocation,
        name: rawLocation.name ?? ""
    };
}

function normalizeLocationSearchResult(rawResult) {
    return {
        ...rawResult,
        name: rawResult.name ?? "",
        label: rawResult.label ?? buildLocationSearchLabel(rawResult)
    };
}

function buildLocationSearchLabel(result) {
    const name = result.name ?? "";
    const region = result.region?.trim();
    const country = result.country ?? "";

    return region
        ? `${name}, ${region}, ${country}`
        : `${name}, ${country}`;
}

function getDateKey(date) {
    const year = date.getFullYear();
    const month = String(date.getMonth() + 1).padStart(2, "0");
    const day = String(date.getDate()).padStart(2, "0");

    return `${year}-${month}-${day}`;
}

function formatDayName(date) {
    return date.toLocaleDateString("pl-PL", { weekday: "long" });
}

function formatShortDate(date) {
    const day = String(date.getDate()).padStart(2, "0");
    const month = String(date.getMonth() + 1).padStart(2, "0");

    return `${day}.${month}`;
}

function formatRelativeDayLabel(date) {
    const today = new Date();
    const current = new Date(today.getFullYear(), today.getMonth(), today.getDate());
    const target = new Date(date.getFullYear(), date.getMonth(), date.getDate());

    const diffMs = target - current;
    const diffDays = Math.round(diffMs / 86400000);

    if (diffDays === 0) return t("relativeDays.today");
    if (diffDays === 1) return t("relativeDays.tomorrow");
    if (diffDays === -1) return t("relativeDays.yesterday");

    return "";
}

function formatHour(value) {
    if (!value) {
        return "-";
    }

    const date = new Date(value);

    return date.toLocaleTimeString("pl-PL", {
        hour: "2-digit",
        minute: "2-digit"
    });
}

function formatNumber(value, digits = 1) {
    if (value === null || value === undefined) {
        return "-";
    }

    return Number(value).toFixed(digits);
}

function formatPercentValue(value) {
    if (value === null || value === undefined) {
        return "-";
    }

    return Math.round(Number(value) * 100);
}

function formatWind(value) {
    if (value === null || value === undefined) {
        return "-";
    }

    const kmh = Number(value) * 3.6;
    return kmh.toFixed(1);
}

function buildLocationLabel(location) {
    const name = location.name ?? "";
    const region = location.region?.trim();
    const country = location.country ?? "";

    if (region) {
        return `
            <span class="location-main">${escapeHtml(name)}</span>
            <span class="location-meta"> (${escapeHtml(region)}, ${escapeHtml(country)})</span>
        `;
    }

    return `
        <span class="location-main">${escapeHtml(name)}</span>
        <span class="location-meta"> (${escapeHtml(country)})</span>
    `;
}

function getTemperatureClass(value) {
    if (value === null || value === undefined) {
        return "";
    }

    return Number(value) < 0 ? "value-cold" : "value-warm";
}

function getRainClass(value) {
    if (value === null || value === undefined || Number(value) === 0) {
        return "value-muted";
    }

    return "value-rain";
}

function getSnowClass(value) {
    if (value === null || value === undefined || Number(value) === 0) {
        return "value-muted";
    }

    return "value-snow";
}

function getPrecipitationClass(value) {
    if (value === null || value === undefined || Number(value) === 0) {
        return "value-muted";
    }

    return "value-rain";
}

function escapeHtml(value) {
    return String(value)
        .replaceAll("&", "&amp;")
        .replaceAll("<", "&lt;")
        .replaceAll(">", "&gt;")
        .replaceAll('"', "&quot;")
        .replaceAll("'", "&#39;");
}

/* ----------------------------- UI helpers ------------------------------- */

function clearMessage() {
    messageBox.textContent = "";
    messageBox.className = "";
}

function showMessage(message, isError) {
    messageBox.textContent = message;
    messageBox.className = isError ? "message-error" : "message-success";
}

function setFormBusy(isBusy) {
    if (submitBtn) {
        submitBtn.disabled = isBusy;
    }

    if (cancelEditBtn) {
        cancelEditBtn.disabled = isBusy;
    }

    if (reloadAllBtn) {
        reloadAllBtn.disabled = isBusy;
    }
}

function setSearchBusy(isBusy) {
    const locationSearchBtn = document.getElementById("location-search-btn");
    const locationSearchCancelBtn = document.getElementById("location-search-cancel-btn");

    if (locationSearchBtn) {
        locationSearchBtn.disabled = isBusy;
    }

    if (locationSearchCancelBtn) {
        locationSearchCancelBtn.disabled = isBusy;
    }
}

/* ---------------------------- Error handling ---------------------------- */

async function tryReadError(response) {
    try {
        const error = await response.json();
        return error.message || t("errors.generic");
    } catch {
        return t("errors.generic");
    }
}
