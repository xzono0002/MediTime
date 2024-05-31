#include "fb.h"

DefaultNetwork network;
UserAuth user_auth(API_KEY, USER_EMAIL, USER_PASSWORD);
FirebaseApp app;

#if defined(ESP32) || defined(ESP8266) || defined(ARDUINO_RASPBERRY_PI_PICO_W)
WiFiClientSecure ssl_client;
#elif defined(ARDUINO_ARCH_SAMD) || defined(ARDUINO_UNOWIFIR4) || defined(ARDUINO_GIGA) || defined(ARDUINO_PORTENTA_C33) || defined(ARDUINO_NANO_RP2040_CONNECT)
WiFiSSLClient ssl_client;
#endif

AsyncClient aClient(ssl_client, getNetwork(network));
RealtimeDatabase Database;
AsyncResult aResult_no_callback;

WiFiUDP Udp;
NTPClient timeClient(Udp, "time.nist.gov", 0, 60000);

const char* ntpServers[] = {"time.nist.gov", "pool.ntp.org", "time.google.com"};
const int ntpServerCount = sizeof(ntpServers) / sizeof(ntpServers[0]);

String userUID;
bool isWifiConnected = false;

/**************************************************************************/
/*      Firebase Aux       */
/**************************************************************************/
void authHandler() {
    unsigned long ms = millis();
    while (app.isInitialized() && !app.ready() && millis() - ms < 120 * 1000) {
        JWT.loop(app.getAuth());
        printResult(aResult_no_callback);
    }
}

void printResult(AsyncResult& aResult) {
    if (aResult.isEvent()) {
        Firebase.printf("Event task: %s, msg: %s, code: %d\n", aResult.uid().c_str(), aResult.appEvent().message().c_str(), aResult.appEvent().code());
    }

    if (aResult.isDebug()) {
        Firebase.printf("Debug task: %s, msg: %s\n", aResult.uid().c_str(), aResult.debug().c_str());
    }

    if (aResult.isError()) {
        Firebase.printf("Error task: %s, msg: %s, code: %d\n", aResult.uid().c_str(), aResult.error().message().c_str(), aResult.error().code());
    }
}

void printError(int code, const String& msg) {
    Firebase.printf("Error, msg: %s, code: %d\n", msg.c_str(), code);
}

/**************************************************************************/
/*      Date, Time, and Day       */
/**************************************************************************/
void getDateNow(char* buffer, RTCTime& currentTime) {
    sprintf(buffer, "%02d-%02d-%04d",
            Month2int(currentTime.getMonth()),
            currentTime.getDayOfMonth(),
            currentTime.getYear());
}

void getTimeNow(char* buffer, RTCTime& currentTime) {
    sprintf(buffer, "%02d:%02d:%02d",
            currentTime.getHour(),
            currentTime.getMinutes(),
            currentTime.getSeconds());
}

int getDayNow(RTCTime& currentTime) {
    return static_cast<int>(currentTime.getDayOfWeek()) - 1;
}

bool isDayInString(int currentDay, const char* dayString) {
    String dayStr = String(dayString);
    int startIndex = 0;
    int endIndex = 0;
    while ((endIndex = dayStr.indexOf(',', startIndex)) != -1) {
        String day = dayStr.substring(startIndex, endIndex);
        if (day.toInt() == currentDay) {
            return true;
        }
        startIndex = endIndex + 1;
    }
    if (dayStr.substring(startIndex).toInt() == currentDay) {
        return true;
    }
    return false;
}

/**************************************************************************/
/*      Setup      */
/**************************************************************************/
void printWifiStatus() {
    Serial.print("SSID: ");
    Serial.println(WiFi.SSID());
    IPAddress ip = WiFi.localIP();
    Serial.print("IP Address: ");
    Serial.println(ip);
    long rssi = WiFi.RSSI();
    Serial.print("Signal strength (RSSI):");
    Serial.print(rssi);
    Serial.println(" dBm");
}

void connectToWiFi() {
    if (WiFi.status() == WL_NO_MODULE) {
        Serial.println("Communication with WiFi module failed!");
        while (true);
    }

    unsigned long startAttemptTime = millis();
    const unsigned long wifiTimeout = 10000;
    Serial.print("Attempting to connect to SSID: ");
    Serial.println(WIFI_SSID);
    while (WiFi.status() != WL_CONNECTED && (millis() - startAttemptTime) < wifiTimeout) {
        WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
        Serial.print(".");
        delay(1000);
    }

    if (WiFi.status() == WL_CONNECTED) {
        isWifiConnected = true;
        Serial.println("Connected to WiFi");
        printWifiStatus();
    } else {
        isWifiConnected = false;
        Serial.println("Failed to connect to WiFi within the timeout period.");
    }
}

void setupFirebase() {
    Serial.begin(9600);
    while (!Serial);

    delay(3000);

    connectToWiFi();
    RTC.begin();

    if (isWifiConnected) {
        initializeApp(aClient, app, getAuth(user_auth), aResult_no_callback);
        authHandler();
        app.getApp<RealtimeDatabase>(Database);
        Database.url(DATABASE_URL);

        userUID = String(app.getUid());

        Serial.println("\nStarting connection to NTP server...");
        timeClient.begin();

        bool ntpSuccess = false;

        for (int i = 0; i < ntpServerCount; i++) {
            timeClient.setPoolServerName(ntpServers[i]);
            timeClient.begin();

            int retries = 5;
            while (!timeClient.update() && retries > 0) {
                Serial.print("Failed to get NTP time from ");
                Serial.print(ntpServers[i]);
                Serial.println(", retrying...");
                delay(1000);
                retries--;
            }

            if (retries > 0) {
                ntpSuccess = true;
                Serial.print("Successfully got NTP time from ");
                Serial.println(ntpServers[i]);
                break;
            }
        }

        if (!ntpSuccess) {
            Serial.println("Failed to get NTP time after multiple retries with different servers.");
            return;
        }

        int timeZoneOffsetHours = 8;
        unsigned long unixTime = timeClient.getEpochTime() + (timeZoneOffsetHours * 3600);
        Serial.print("Unix time = ");
        Serial.println(unixTime);

        RTCTime timeToSet(unixTime);
        RTC.setTime(timeToSet);
    }

    RTCTime currentTime;
    RTC.getTime(currentTime);
    char dateBuffer[11];
    char timeBuffer[9];
    getDateNow(dateBuffer, currentTime);
    getTimeNow(timeBuffer, currentTime);
    Serial.print(dateBuffer);
    Serial.print("T");
    Serial.println(timeBuffer);
}
