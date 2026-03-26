const cityForm = document.getElementById("city-form");
const tableBody = document.getElementById("cities-table-body");
const messageBox = document.getElementById("message");
const reloadAllBtn = document.getElementById("reload-all-btn");

let citiesState = [];

document.addEventListener("DOMContentLoaded", async () => {
    await loadCities();
});

reloadAllBtn.addEventListener("click", async () => {
    await loadCities();
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

async function loadCities() {
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
        <td>${formatValue(weather?.temperature)}</td>
        <td>${formatValue(weather?.feelsLike)}</td>
        <td>${formatValue(weather?.humidity)}</td>
        <td>${formatValue(weather?.pressure)}</td>
        <td>${formatWind(weather?.windSpeed)}</td>
        <td>${formatValue(weather?.cloudsPercentage)}</td>
        <td>${weather?.description ?? "-"}</td>
        <td>
            <div class="order-actions">
                <button class="order-btn move-up-btn" title="Move up">↑</button>
                <button class="order-btn move-down-btn" title="Move down">↓</button>
            </div>
        </td>
        <td>
            <div class="actions">
                <button class="action-btn refresh-btn">Refresh</button>
                <button class="action-btn delete-btn">Delete</button>
            </div>
        </td>
    `;

    tr.querySelector(".refresh-btn").addEventListener("click", async () => {
        try {
            await refreshSingleCity(city.id);
            showMessage(`Weather refreshed for ${city.city}`, false);
        } catch (error) {
            showMessage(error.message, true);
        }
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

function formatValue(value) {
    if (value === null || value === undefined) {
        return "-";
    }
    return value;
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