(function () {
    const APP_TEXT = {
        app: {
            title: "Weather 2.0",
            subtitle: "Current weather overview and forecast for saved locations",
            footerAuthor: "Created by Jakub Heppner",
            footerVersionLabel: "App version 2.2"
        },

        auth: {
            loginPageTitle: "Login - Weather 2.0",
            loginLink: "Login",
            logoutLink: "Logout",
            loginTitle: "Please sign in",
            loginSubtitle: "Sign in to manage saved locations in Weather 2.0.",
            usernameLabel: "Username",
            passwordLabel: "Password",
            signInButton: "Sign in",
            invalidCredentials: "Invalid username or password.",
            loggedOut: "You have been logged out."
        },

        sections: {
            savedLocations: "Saved locations",
            manageLocations: "Manage locations"
        },

        buttons: {
            reloadList: "Reload list",
            search: "Search",
            cancel: "Cancel",
            cancelEdit: "Cancel edit",
            addLocation: "Add location",
            saveChanges: "Save changes",
            forecast: "Forecast",
            edit: "Edit",
            delete: "Delete",
            moveUp: "Move up",
            moveDown: "Move down"
        },

        labels: {
            location: "Location",
            name: "Name or label",
            region: "Region",
            country: "Country",
            longitude: "Longitude",
            latitude: "Latitude",
            actions: "Actions",
            temperatureShort: "Temp.",
            feelsLikeShort: "Feels like",
            humidity: "Humidity",
            pressure: "Pressure",
            windSpeed: "Wind sp.",
            clouds: "Clouds",
            descriptionShort: "Desc.",
            description: "Description",
            day: "Day",
            hour: "Hour",
            wind: "Wind",
            rain: "Rain",
            snow: "Snow",
            precipitationChance: "Precip. chance"
        },

        units: {
            temperature: "°C",
            humidity: "%",
            pressure: "hPa",
            windSpeed: "km/h",
            clouds: "%",
            rain: "mm",
            snow: "mm",
            precipitationChance: "%"
        },

        placeholders: {
            locationSearch: "e.g. Wroclaw, Gdańsk..",
            name: "required",
            region: "optional",
            country: "required",
            longitude: "e.g. 21.0122",
            latitude: "e.g. 52.2297"
        },

        hints: {
            locationSearch:
                "Search for a location to fill the form automatically. You can edit the details before saving."
        },

        messages: {
            weatherDataRefreshed: "Weather data refreshed.",
            locationAdded: "Location added successfully.",
            locationUpdated: "Location updated successfully.",
            locationDeleted: "Location deleted successfully.",
            displayOrderUpdated: "Display order updated.",
            formFilledFromSearch: "Selected location loaded into the form."
        },

        errors: {
            generic: "Something went wrong. Please try again.",
            requestFailed: "The request could not be completed. Please try again.",
            forecastLoadFailed: "Could not load forecast.",
            noForecastData: "No forecast data",
            noSearchResults: "No results"
        },

        confirmations: {
            deleteLocation: 'Delete location "{name}"? This action cannot be undone.'
        },

        weather: {
            loadingForecast: "Loading forecast..."
        },

        relativeDays: {
            today: "today",
            tomorrow: "tomorrow",
            yesterday: "yesterday"
        }
    };

    function getByPath(obj, path) {
        return path.split(".").reduce((current, key) => current?.[key], obj);
    }

    function formatTemplate(template, params = {}) {
        return String(template).replace(/\{(\w+)}/g, function (_, key) {
            return params[key] !== undefined && params[key] !== null
                ? String(params[key])
                : "{" + key + "}";
        });
    }

    function t(path, params) {
        const value = getByPath(APP_TEXT, path);

        if (typeof value !== "string") {
            return path;
        }

        return formatTemplate(value, params);
    }

    window.APP_TEXT = Object.freeze(APP_TEXT);
    window.t = t;
})();
