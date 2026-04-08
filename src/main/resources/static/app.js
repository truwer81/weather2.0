const cityForm = document.getElementById("city-form");
const tableBody = document.getElementById("cities-table-body");
const messageBox = document.getElementById("message");
const reloadAllBtn = document.getElementById("reload-all-btn");
const searchContainer = document.getElementById("location-search-container");
const editingIdInput = document.getElementById("editing-id");
const submitBtn = document.getElementById("submit-btn");
const cancelEditBtn = document.getElementById("cancel-edit-btn");
const loginLink = document.getElementById("login-link");
const logoutForm = document.getElementById("logout-form");

let citiesState = [];
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
    await loadCities();
});

/* ------------------------------ UI events ------------------------------- */

reloadAllBtn.addEventListener("click", async () => {
    try {
        setFormBusy(true);
        await loadCities();
        showMessage("List reloaded. Fresh weather data retrieved.", false);
    } catch (error) {
        showMessage(error.message, true);
    } finally {
        setFormBusy(false);
    }
});

cityForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const editingId = editingIdInput.value;

    const payload = getCityFormValues();

    const isEdit = Boolean(editingId);
    const url = isEdit ? `/api/cities/${editingId}` : "/api/cities";
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
        showMessage(isEdit ? "Location updated successfully" : "Location added successfully", false);
        await loadCities();
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
    const formPanel = document.getElementById("city-form-panel");

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

/* -------------------------- Cities data loading ------------------------- */

async function loadCities() {
    clearMessage();

    try {
        const response = await fetch("/api/cities");

        if (!response.ok) {
            const errorMessage = await tryReadError(response);
            showMessage(errorMessage, true);
            return;
        }

        const cities = await response.json();

        citiesState = cities
            .map((city, index) => ({
                ...city,
                sortOrder: city.sortOrder ?? index + 1
            }))
            .sort((a, b) => a.sortOrder - b.sortOrder);

        await renderCities();
    } catch (error) {
        showMessage(error.message, true);
    }
}

async function renderCities() {
    tableBody.innerHTML = "";

    for (const city of citiesState) {
        const weather = await loadWeather(city.id);
        const row = buildRow(city, weather);
        tableBody.appendChild(row);
    }
}

async function loadWeather(cityId) {
    try {
        const response = await fetch(`/api/weather?cityId=${cityId}`);

        if (!response.ok) {
            return null;
        }

        return await response.json();
    } catch {
        return null;
    }
}

/* ----------------------------- City actions ----------------------------- */

function buildRow(city, weather) {
    const tr = document.createElement("tr");
    tr.className = "location-row";

    tr.innerHTML = `
        <td class="col-location-cell" data-label="Location">
            <div class="cell-value location-value">
                ${buildLocationLabel(city)}
            </div>
        </td>
        <td class="col-num-cell" data-label="Temp.">
            <span class="cell-value">${formatNumber(weather?.temperature, 1)}</span>
        </td>
        <td class="col-num-cell" data-label="Feels like">
            <span class="cell-value">${formatNumber(weather?.feelsLike, 1)}</span>
        </td>
        <td class="col-num-cell" data-label="Humidity">
            <span class="cell-value">${formatNumber(weather?.humidity, 0)}</span>
        </td>
        <td class="col-num-cell" data-label="Pressure">
            <span class="cell-value">${formatNumber(weather?.pressure, 0)}</span>
        </td>
        <td class="col-num-cell" data-label="Wind speed">
            <span class="cell-value">${formatWind(weather?.windSpeed)}</span>
        </td>
        <td class="col-num-cell" data-label="Clouds">
            <span class="cell-value">${formatNumber(weather?.cloudsAll ?? weather?.cloudsPercentage, 0)}</span>
        </td>
        <td class="col-desc-cell" data-label="Desc.">
            <span class="cell-value desc-value">${weather?.description ?? "-"}</span>
        </td>
        <td class="col-actions-cell" data-label="Actions">
            <div class="cell-value actions-value">
                ${buildActionsHtml()}
            </div>
        </td>
    `;

    tr.querySelector(".forecast-btn")?.addEventListener("click", async () => {
        await toggleForecastRow(tr, city.id);
    });

    tr.querySelector(".edit-btn")?.addEventListener("click", () => {
        startEdit(city);
    });

    tr.querySelector(".delete-btn")?.addEventListener("click", async () => {
        const confirmed = window.confirm(
            `Delete location "${city.city ?? "this location"}"? This action cannot be undone.`
        );

        if (!confirmed) {
            return;
        }

        try {
            const response = await fetch(`/api/cities/${city.id}`, {
                method: "DELETE"
            });

            if (!response.ok) {
                const errorMessage = await tryReadError(response);
                showMessage(errorMessage, true);
                return;
            }

            showMessage("City deleted successfully", false);
            await loadCities();
        } catch (error) {
            showMessage(error.message, true);
        }
    });

    tr.querySelector(".move-up-btn")?.addEventListener("click", async () => {
        await moveCity(city.id, -1);
    });

    tr.querySelector(".move-down-btn")?.addEventListener("click", async () => {
        await moveCity(city.id, 1);
    });

    return tr;
}

function getCityFormValues() {
    return {
        city: document.getElementById("city").value.trim(),
        country: document.getElementById("country").value.trim(),
        region: document.getElementById("region").value.trim() || null,
        longitude: parseFloat(document.getElementById("longitude").value),
        latitude: parseFloat(document.getElementById("latitude").value)
    };
}

function startEdit(city) {
    resetLocationSearch(true);

    editingIdInput.value = city.id;
    document.getElementById("city").value = city.city ?? "";
    document.getElementById("country").value = city.country ?? "";
    document.getElementById("region").value = city.region ?? "";
    document.getElementById("longitude").value = city.longitude ?? "";
    document.getElementById("latitude").value = city.latitude ?? "";

    submitBtn.textContent = "Save changes";
    cancelEditBtn.hidden = false;

    cityForm.scrollIntoView({
        behavior: "smooth",
        block: "start"
    });
}

function resetFormMode() {
    editingIdInput.value = "";
    cityForm.reset();
    submitBtn.textContent = "Add location";
    cancelEditBtn.hidden = true;
}

async function moveCity(cityId, direction) {
    const currentIndex = citiesState.findIndex((city) => city.id === cityId);

    if (currentIndex === -1) {
        return;
    }

    const targetIndex = currentIndex + direction;

    if (targetIndex < 0 || targetIndex >= citiesState.length) {
        return;
    }

    [citiesState[currentIndex], citiesState[targetIndex]] =
        [citiesState[targetIndex], citiesState[currentIndex]];

    citiesState = citiesState.map((city, index) => ({
        ...city,
        sortOrder: index + 1
    }));

    await renderCities();

    try {
        await persistSortOrder();
        showMessage("Display order updated", false);
    } catch (error) {
        showMessage(error.message, true);
        await loadCities();
    }
}

function buildActionsHtml() {
    if (!canManageLocations()) {
        return `
            <div class="actions">
                <button type="button" class="btn btn-primary btn-md btn-action action-btn forecast-btn">Forecast</button>
            </div>
        `;
    }

    return `
        <div class="actions">
            <div class="order-actions">
                <button type="button" class="btn btn-secondary btn-icon order-btn move-up-btn" title="Move up">↑</button>
                <button type="button" class="btn btn-secondary btn-icon order-btn move-down-btn" title="Move down">↓</button>
            </div>

            <button type="button" class="btn btn-primary btn-md btn-action action-btn forecast-btn">Forecast</button>
            <button type="button" class="btn btn-warning btn-md btn-action action-btn edit-btn">Edit</button>
            <button type="button" class="btn btn-danger btn-md btn-action action-btn delete-btn">Delete</button>
        </div>
    `;
}

async function persistSortOrder() {
    const payload = citiesState.map((city, index) => ({
        localizationId: city.id,
        sortOrder: index + 1
    }));

    const response = await fetch("/api/cities/order", {
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

    const updatedCities = await response.json();

    citiesState = updatedCities
        .map((city, index) => ({
            ...city,
            sortOrder: city.sortOrder ?? index + 1
        }))
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
                <p class="location-search-hint">
                    Search for a location and pick one to fill the form automatically.
                    You can edit the details before saving or enter your own data manually.
                </p>
            </div>

            <div class="location-search-box">
                <input
                    type="text"
                    id="location-search-input"
                    placeholder="e.g. Wroclaw, Gdańsk.."
                >

                <div class="location-search-actions">
                    <button type="button" id="location-search-btn" class="btn btn-primary btn-md">Search</button>
                    <button
                        type="button"
                        id="location-search-cancel-btn"
                        class="btn btn-secondary btn-md"
                        hidden
                    >
                        Cancel
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

        const results = await response.json();

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
        container.innerHTML = `<div class="location-search-empty">No results</div>`;
        return;
    }

    container.innerHTML = results
        .map((result) => `
            <div
                class="location-result-item"
                data-city="${result.city}"
                data-region="${result.region ?? ""}"
                data-country="${result.country}"
                data-lat="${result.latitude}"
                data-lon="${result.longitude}"
            >
                <div class="location-result-label">${result.label}</div>
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
            document.getElementById("city").value = element.dataset.city;
            document.getElementById("region").value = element.dataset.region;
            document.getElementById("country").value = element.dataset.country;
            document.getElementById("latitude").value = element.dataset.lat;
            document.getElementById("longitude").value = element.dataset.lon;

            submitBtn.textContent = "Add location";
            cancelEditBtn.hidden = false;

            showMessage("Location filled from search", false);

            const resultsContainer = document.getElementById("location-search-results");
            if (resultsContainer) {
                resultsContainer.hidden = true;
            }

            document.getElementById("city-form").scrollIntoView({
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

async function toggleForecastRow(cityRow, cityId) {
    const nextRow = cityRow.nextElementSibling;

    if (nextRow && nextRow.classList.contains("forecast-details-row")) {
        nextRow.remove();
        return;
    }

    const detailsRow = document.createElement("tr");
    detailsRow.className = "forecast-details-row";
    detailsRow.innerHTML = `
        <td colspan="9">
            <div class="forecast-loading">Loading forecast...</div>
        </td>
    `;

    cityRow.insertAdjacentElement("afterend", detailsRow);

    try {
        const forecast = await loadForecast(cityId);
        detailsRow.innerHTML = `
            <td colspan="9">
                ${buildForecastTable(forecast)}
            </td>
        `;
    } catch (error) {
        detailsRow.innerHTML = `
            <td colspan="9">
                <div class="message-error">Could not load forecast: ${error.message}</div>
            </td>
        `;
    }
}

async function loadForecast(cityId) {
    const response = await fetch(`/api/weather/forecast?cityId=${cityId}`);

    if (!response.ok) {
        const error = await tryReadError(response);
        throw new Error(error);
    }

    return await response.json();
}

function buildForecastTable(items) {
    if (!items || items.length === 0) {
        return `<div class="forecast-empty">No forecast data</div>`;
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
                                <div class="forecast-day-name">${group.dayName}</div>
                                <div class="forecast-day-label">${group.relativeLabel}</div>
                                <div class="forecast-day-date">(${group.shortDate})</div>
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
                            <td>${item.description ?? "-"}</td>
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
                                <span class="th-title">Day</span>
                            </div>
                        </th>
                        <th>
                            <div class="th-stack">
                                <span class="th-title">Hour</span>
                            </div>
                        </th>
                        <th>
                            <div class="th-stack">
                                <span class="th-title">Temp.</span>
                                <span class="th-unit">°C</span>
                            </div>
                        </th>
                        <th>
                            <div class="th-stack">
                                <span class="th-title">Feels<br>like</span>
                                <span class="th-unit">°C</span>
                            </div>
                        </th>
                        <th>
                            <div class="th-stack">
                                <span class="th-title">Wind</span>
                                <span class="th-unit">km/h</span>
                            </div>
                        </th>
                        <th>
                            <div class="th-stack">
                                <span class="th-title">Rain</span>
                                <span class="th-unit">mm</span>
                            </div>
                        </th>
                        <th>
                            <div class="th-stack">
                                <span class="th-title">Snow</span>
                                <span class="th-unit">mm</span>
                            </div>
                        </th>
                        <th>
                            <div class="th-stack">
                                <span class="th-title">Precip.<br>chance</span>
                                <span class="th-unit">%</span>
                            </div>
                        </th>
                        <th>
                            <div class="th-stack">
                                <span class="th-title">Description</span>
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

    if (diffDays === 0) return "dzisiaj";
    if (diffDays === 1) return "jutro";
    if (diffDays === -1) return "wczoraj";

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

function buildLocationLabel(city) {
    const cityName = city.city ?? "";
    const region = city.region?.trim();
    const country = city.country ?? "";

    if (region) {
        return `
            <span class="location-main">${cityName}</span>
            <span class="location-meta"> (${region}, ${country})</span>
        `;
    }

    return `
        <span class="location-main">${cityName}</span>
        <span class="location-meta"> (${country})</span>
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
        return error.message || "Request failed";
    } catch {
        return `Request failed with status ${response.status}`;
    }
}