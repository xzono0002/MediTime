#ifndef FB_H
#define FB_H

#include <Arduino.h>
#include <FirebaseClient.h>
#include <NTPClient.h>
#include <WiFiUdp.h>

#include "RTC.h"

#if defined(ESP32) || defined(ARDUINO_RASPBERRY_PI_PICO_W) || defined(ARDUINO_GIGA)
#include <WiFi.h>
#elif defined(ESP8266)
#include <ESP8266WiFi.h>
#elif __has_include(<WiFiNINA.h>) || defined(ARDUINO_NANO_RP2040_CONNECT)
#include <WiFiNINA.h>
#elif __has_include(<WiFi101.h>)
#include <WiFi101.h>
#elif __has_include(<WiFiS3.h>) || defined(ARDUINO_UNOWIFIR4)
#include <WiFiS3.h>
#elif __has_include(<WiFiC3.h>) || defined(ARDUINO_PORTENTA_C33)
#include <WiFiC3.h>
#elif __has_include(<WiFi.h>)
#include <WiFi.h>
#endif

// #define WIFI_SSID "HUAWEI-2.4G-U8g6"
// #define WIFI_PASSWORD "BAzu9S6w"

#define WIFI_SSID "Redmi Note 12"
#define WIFI_PASSWORD "siomai123"

#define DATABASE_URL "meditime-bbeb6-default-rtdb.asia-southeast1.firebasedatabase.app"
#define API_KEY "AIzaSyC5AIElJgQ0Q9G7XpMYW2wyGHoRmAAXlws"
#define USER_EMAIL "sophia.granado12@gmail.com"
#define USER_PASSWORD "sophia@MediTime"

extern DefaultNetwork network;
extern UserAuth user_auth;
extern FirebaseApp app;

#if defined(ESP32) || defined(ESP8266) || defined(ARDUINO_RASPBERRY_PI_PICO_W)
#include <WiFiClientSecure.h>
extern WiFiClientSecure ssl_client;
#elif defined(ARDUINO_ARCH_SAMD) || defined(ARDUINO_UNOWIFIR4) || defined(ARDUINO_GIGA) || defined(ARDUINO_PORTENTA_C33) || defined(ARDUINO_NANO_RP2040_CONNECT)
#include <WiFiSSLClient.h>
extern WiFiSSLClient ssl_client;
#endif

using AsyncClient = AsyncClientClass;
extern AsyncClient aClient;
extern RealtimeDatabase Database;
extern AsyncResult aResult_no_callback;

extern WiFiUDP Udp;
extern NTPClient timeClient;

extern const char* ntpServers[];
extern const int ntpServerCount;

extern String userUID;
extern bool isWifiConnected;

void authHandler();
void printResult(AsyncResult& aResult);
void printError(int code, const String& msg);
void printWifiStatus();
void connectToWiFi();
void getDateNow(char* buffer, RTCTime& currentTime);
void getTimeNow(char* buffer, RTCTime& currentTime);
int getDayNow(RTCTime& currentTime);
bool isDayInString(int currentDay, const char* dayString);
void setupFirebase();

#endif  // FB_H