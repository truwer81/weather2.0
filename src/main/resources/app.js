const cityForm = document.getElementById("city-form");
const tableBody = document.getElementById("cities-table-body");
const messageBox = document.getElementById("message");

document.addEventListener("DOMContentLoaded", () => {
    loadCities();
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
            const error = await response.json();
            throw new Error(error.message || "Failed to add city");
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
            throw new Error("Failed to load cities");
        }

        const cities = await response.json();
        tableBody.innerHTML = "";

        for (const city of cities) {
            const weather = await loadWeather(city.id);
            const row = buildRow(city, weather);
            tableBody.appendChild(row);
        }
    } catch (error) {
        showMessage(error.message, true);
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
        <td>${city.id}</td>
        <td>${city.city}</td>
        <td>${city.country}</td>
        <td>${city.region ?? ""}</td>
        <td>${city.longitude}</td>
        <td>${city.latitude}</td>
        <td>${weather?.temperature ?? "-"}</td>
        <td>${weather?.description ?? "-"}</td>
        <td>
            <div class="actions">
                <button data-id="${city.id}" class="refresh-btn">Refresh weather</button>
                <button data-id="${city.id}" class="delete-btn">Delete</button>
            </div>
        </td>
    `;

    tr.querySelector(".refresh-btn").addEventListener("click", async () => {
        try {
            await loadWeather(city.id);
            await loadCities();
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
                const error = await response.json();
                throw new Error(error.message || "Failed to delete city");
            }

            showMessage("City deleted successfully", false);
            await loadCities();
        } catch (error) {
            showMessage(error.message, true);
        }
    });

    return tr;
}

function showMessage(message, isError) {
    messageBox.textContent = message;
    messageBox.className = isError ? "message-error" : "message-success";
}