const cityForm = document.getElementById("city-form");
const tableBody = document.getElementById("cities-table-body");
const messageBox = document.getElementById("message");
const reloadAllBtn = document.getElementById("reload-all-btn");

let citiesState = [];

console.log("NEW APP.JS LOADED");

document.addEventListener("DOMContentLoaded", async () => {
    await loadCities();
});

reloadAllBtn.addEventListener("click", async () => {
    try {
        await loadCities();
        showMessage("List reloaded. Fresh weather data retrieved.", false);
    } catch (error) {
        showMessage(error.message, true);
    }
});

cityForm.addEventListener("submit", async (event) => {
    event.preventDefault();

    const payload = {
        city: document.getElementById("city").value,
        country: document.getElementById("country").value,
        region: document.getElementById("region").value,
        longitude: parseFloat(document.getElementById("longitude").value),
        latitude: parseFloat(document.getElementById("latitude").value)
    };

    try {
        const response = await fetch("/api/cities", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            body: JSON.stringify(payload)
        });

        if (!response.ok) {
            const error = await tryReadError(response);
            throw new Error(error);
        }

        cityForm.reset();
        showMessage("City added successfully", false);
        await loadCities();
    } catch (error) {
        showMessage(error.message, true);
    }
});

function clearMessage() {
    messageBox.textContent = "";
    messageBox.className = "";
}

async function loadCities() {
    clearMessage();
    try {
        const response = await fetch("/api/cities");

        if (!response.ok) {
            const error = await tryReadError(response);
            throw new Error(error);
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

function buildRow(city, weather) {
    const tr = document.createElement("tr");

    tr.innerHTML = `
        <td>${city.city ?? ""}</td>
        <td>${city.region ?? ""}</td>
        <td>${city.country ?? ""}</td>
        <td>${formatNumber(weather?.temperature, 1)}</td>
        <td>${formatNumber(weather?.feelsLike, 1)}</td>
        <td>${formatNumber(weather?.humidity, 0)}</td>
        <td>${formatNumber(weather?.pressure, 0)}</td>
        <td>${formatWind(weather?.windSpeed)}</td>
        <td>${formatNumber(weather?.cloudsAll ?? weather?.cloudsPercentage, 0)}</td>
        <td>${weather?.description ?? "-"}</td>
        <td>
            <div class="order-actions">
                <button class="order-btn move-up-btn" title="Move up">↑</button>
                <button class="order-btn move-down-btn" title="Move down">↓</button>
            </div>
        </td>
        <td>
            <div class="actions">
                <button class="action-btn forecast-btn">Forecast</button>
                <button class="action-btn edit-btn">Edit location</button>
                <button class="action-btn delete-btn">Delete</button>
            </div>
        </td>
    `;

    tr.querySelector(".forecast-btn").addEventListener("click", async () => {
        await toggleForecastRow(tr, city.id);
    });

    tr.querySelector(".edit-btn").addEventListener("click", () => {
        showMessage("Edit location will be available in a future update.", false);
    });

    tr.querySelector(".delete-btn").addEventListener("click", async () => {
        try {
            const response = await fetch(`/api/cities/${city.id}`, {
                method: "DELETE"
            });

            if (!response.ok) {
                const error = await tryReadError(response);
                throw new Error(error);
            }

            showMessage("City deleted successfully", false);
            await loadCities();
        } catch (error) {
            showMessage(error.message, true);
        }
    });

    tr.querySelector(".move-up-btn").addEventListener("click", async () => {
        await moveCity(city.id, -1);
    });

    tr.querySelector(".move-down-btn").addEventListener("click", async () => {
        await moveCity(city.id, 1);
    });

    return tr;
}

async function toggleForecastRow(cityRow, cityId) {
    const nextRow = cityRow.nextElementSibling;

    if (nextRow && nextRow.classList.contains("forecast-details-row")) {
        nextRow.remove();
        return;
    }

    const detailsRow = document.createElement("tr");
    detailsRow.className = "forecast-details-row";
    detailsRow.innerHTML = `
        <td colspan="12">
            <div class="forecast-loading">Loading forecast...</div>
        </td>
    `;

    cityRow.insertAdjacentElement("afterend", detailsRow);

    try {
        const forecast = await loadForecast(cityId);
        detailsRow.innerHTML = `
            <td colspan="12">
                ${buildForecastTable(forecast)}
            </td>
        `;
    } catch (error) {
        detailsRow.innerHTML = `
            <td colspan="12">
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

    const rows = grouped.map(group => {
        const dayClass = dayIndex % 2 === 0 ? "forecast-day-even" : "forecast-day-odd";
        dayIndex++;

        return group.items.map((item, index) => {
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
        }).join("");
    }).join("");

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

    return Array.from(groups.values()).map(group => ({
        ...group,
        dayName: formatDayName(group.date),
        relativeLabel: formatRelativeDayLabel(group.date),
        shortDate: formatShortDate(group.date)
    }));
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

    if (diffDays === 0) return "dzisiaj";
    if (diffDays === 1) return "jutro";
    if (diffDays === -1) return "wczoraj";

    return "";
}

function formatHour(value) {
    if (!value) return "-";

    const date = new Date(value);

    return date.toLocaleTimeString("pl-PL", {
        hour: "2-digit",
        minute: "2-digit"
    });
}

function formatNumber(value, digits = 1) {
    if (value === null || value === undefined) return "-";
    return Number(value).toFixed(digits);
}

function formatPercentValue(value) {
    if (value === null || value === undefined) return "-";
    return Math.round(Number(value) * 100);
}

function getTemperatureClass(value) {
    if (value === null || value === undefined) return "";
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

async function moveCity(cityId, direction) {
    const currentIndex = citiesState.findIndex(city => city.id === cityId);
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

async function refreshSingleCity(cityId) {
    const response = await fetch(`/api/weather?cityId=${cityId}`);

    if (!response.ok) {
        const error = await tryReadError(response);
        throw new Error(error);
    }

    await renderCities();
}

async function tryReadError(response) {
    try {
        const error = await response.json();
        return error.message || "Request failed";
    } catch {
        return `Request failed with status ${response.status}`;
    }
}

function formatWind(value) {
    if (value === null || value === undefined) {
        return "-";
    }

    const kmh = Number(value) * 3.6;
    return kmh.toFixed(1);
}

function showMessage(message, isError) {
    messageBox.textContent = message;
    messageBox.className = isError ? "message-error" : "message-success";
}