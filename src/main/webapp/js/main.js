var API_URL = '/api';
var ENTRIES_URL = API_URL + '/entries';
var REPORT_URL = API_URL + '/report';
var SETTINGS_URL = API_URL + '/setting';
var CREDENTIALS_COOKIE_NAME = "credentials";
var LOG_FILE_NAME = "log.xlsx";
var CONFIG_FILE_NAME = "data-logger-config.xml";

function setCredentials(credentials) {
    setCookie(CREDENTIALS_COOKIE_NAME, credentials);
}

function getCredentials() {
    return getCookie(CREDENTIALS_COOKIE_NAME);
}

function login() {
    var name = document.getElementById("username").value;
    var pass = document.getElementById("password").value;

    if (name.length > 0 && pass.length > 0) {
        setCredentials(window.btoa(name + ":" + pass));
        initAndLoadEntries();
    } else {
        setStatus("Enter username and  password.")
    }
}

function logout() {
    deleteCookie(CREDENTIALS_COOKIE_NAME);
    initAndLoadEntries();
}

function initAndLoadEntries() {
    hideForm("login-form");
    hideForm("main-form");
    setStatus("Loading...");

    var entriesRequest = new XMLHttpRequest();
    entriesRequest.open('GET', ENTRIES_URL, true);
    entriesRequest.setRequestHeader("Authorization", "Basic" + " " + getCredentials());
    entriesRequest.onreadystatechange = function () {

        setStatus("");
        if (entriesRequest.readyState != 4) return;
        if (entriesRequest.status == 200) {
            showForm("main-form");
            handleResponse(entriesRequest.response);
        }
        else if (entriesRequest.status == 401) {
            showForm("login-form");
        }
        else {
            setStatus("Error " + entriesRequest.status + ". " + entriesRequest.statusText);
        }
    };
    entriesRequest.send();
}

function handleResponse(response) {
    /*Clear table. Deleting all rows except header.*/
    var table = document.getElementById('entries');
    while (table.rows.length > 1) {
        table.deleteRow(1);
    }
    /*Place content*/
    var entries = JSON.parse(response);
    entries.forEach(function (entrie) {
        addRow(entrie.date, entrie.time, entrie.name, entrie.value, entrie.unit);
    })
    drawDashboard(entries);
}

function addRow(date, time, name,value, unit) {
    var table = document.getElementById('entries');
    var row = table.insertRow();
    row.insertCell(0).innerText = date;
    row.insertCell(1).innerText = time;
    row.insertCell(2).innerText = name;
    row.insertCell(3).innerText = value;
    row.insertCell(4).innerText = unit;
}

function downloadReport() {
    downloadFile(REPORT_URL, LOG_FILE_NAME);
}

function downloadConfig() {
    downloadFile(SETTINGS_URL, CONFIG_FILE_NAME);
}

function downloadFile(url, fileName) {
    var xhr = new XMLHttpRequest();
    xhr.responseType = 'blob';
    xhr.onload = function () {
        var tempElement = document.createElement('a');
        tempElement.href = window.URL.createObjectURL(xhr.response);
        tempElement.download = fileName;
        tempElement.style.display = 'none';
        document.body.appendChild(tempElement);
        tempElement.click();
        delete tempElement;
    };
    xhr.open('GET', url, true);
    xhr.setRequestHeader("Authorization", "Basic" + " " + getCredentials());
    xhr.send();
}

function pickFile() {
    document.getElementById("pick-file").click();
}

function uploadFile() {

    var fileUploadRequest = new XMLHttpRequest();

    fileUploadRequest.onload = fileUploadRequest.onerror = function () {
        if (this.status == 200) {
            console.log("Uploading success");
        } else {
            console.log("Uploading error " + this.status);
        }
    };

    fileUploadRequest.open("POST", SETTINGS_URL, true);
    fileUploadRequest.setRequestHeader("Authorization", "Basic" + " " + getCredentials());

    var file = document.getElementById("pick-file").files[0];
    var formData = new FormData();
    formData.append("file", file);

    fileUploadRequest.send(formData);
}

function setStatus(text) {
    /*Hide block if no content*/
    if (text.length > 0)
        showForm("status-bar");
    else
        hideForm("status-bar");

    /*Place text*/
    try {
        document.getElementById("status-bar-text").innerText = text;
    } catch (e) {
        console.log("Error : " + e.name);
    }
}

function showForm(id) {
    document.getElementById(id).style.display = "block";
}

function hideForm(id) {
    document.getElementById(id).style.display = "none";
}

function loginOnEnterPress(event) {
    if (event.keyCode == 13) {
        login();
    }
}