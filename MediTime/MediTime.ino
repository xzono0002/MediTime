#include <LiquidCrystal_I2C.h>
#include <Servo.h>

#include "fb.h"

////////////////////////////////////////////////////////
LiquidCrystal_I2C lcd(0x27, 20, 4);
const int PIN_BUTTON = 2;
const int PIN_BLUE = 4;
const int PIN_GREEN = 5;
const int PIN_RED = 6;
const int PIN_BUZZ = 7;
const int pinStoppers[] = {15, 14, 8, 9, 13, 11, 16, 17, 10, 12,};  // A1, A2, B1, B2, C1, C2, D1, D2, E1, E2
Servo servos[10];
unsigned long preDispenseMillis = 0;
const unsigned long dispenseInterval = 10000;
bool isDispensing = false;
int step = 0;
////////////////////////////////////////////////////////
const char* tubeLabels[5] = {"A", "B", "C", "D", "E"};

// MILLIS
unsigned long preUpdateMillis = 0;
long updateInterval = 60000;

// VARIABLES
// List of node identifiers
const char* nodeIDs[] = {"0", "1", "2", "3", "4"};
const int numNodes = sizeof(nodeIDs) / sizeof(nodeIDs[0]);
int schedNumbers[numNodes];
int pillsOnTube[numNodes];

int triggerDays[numNodes][3];
String triggerTimes[numNodes][3];
int pillQuantities[numNodes][3];

bool isFirstBootDone = false;

void setup() {
    lcd.init();
    lcd.backlight();
    lcd.setCursor(0, 1);
    lcd.print("Connecting to WiFi");

    setupFirebase();

    pinMode(PIN_BUTTON, INPUT_PULLUP);
    pinMode(PIN_BUZZ, OUTPUT);
    pinMode(PIN_RED, OUTPUT);
    pinMode(PIN_BLUE, OUTPUT);
    pinMode(PIN_GREEN, OUTPUT);

    lcd.clear();
    if (!isWifiConnected) {
        while (true) {
            lcd.setCursor(0, 1);
            lcd.print("Connection failed");
            lcd.setCursor(0, 2);
            lcd.print("Please try again.");
            delay(10000);
        }
    }

    lcd.setCursor(0, 1);
    lcd.print("WiFi Connected");

    Serial.println(userUID);

    digitalWrite(PIN_BLUE, HIGH);
    lcd.clear();
    lcd.setCursor(0, 1);
    lcd.print("Getting data...");
    lcd.setCursor(0, 2);
    lcd.print("Do not turn off.");

    getAllSchedule();

    for (int i = 0; i < 10; i++) {
        servos[i].attach(pinStoppers[i]);
        servos[i].write(100);
        delay(150);
        servos[i].detach();
        delay(1000);
    }

    String userName = Database.get<String>(aClient, String("/RegisteredUsers/" + userUID + "/username").c_str());
    if (aClient.lastError().code() == 0) {
        Serial.println(String(userName));
    } else {
        printError(aClient.lastError().code(), aClient.lastError().message());
    }

    lcd.clear();
    digitalWrite(PIN_BLUE, LOW);

    lcd.clear();
    lcd.setCursor(0, 1);
    lcd.print("Welcome ");
    lcd.print(userName);
    delay(5000);

    // Display message on LCD
    digitalWrite(PIN_GREEN, HIGH);
    lcd.clear();
    lcd.setCursor(6, 1);
    lcd.print("MediTime");

    delay(2000);
    lcd.clear();
    lcd.setCursor(6, 1);
    lcd.print("Made By:");

    delay(500);
    lcd.setCursor(6, 2);
    lcd.print("MediTeam");

    delay(5000);

    digitalWrite(PIN_GREEN, LOW);
    lcd.clear();
}

void loop() {
    authHandler();
    Database.loop();

    unsigned long currentMillis = millis();
    if (currentMillis - preUpdateMillis >= updateInterval) {
        preUpdateMillis = currentMillis;
        int connChecker = Database.get<int>(aClient, String("/checker/instance").c_str());
        if (aClient.lastError().code() == 0) {
            Serial.print("[isStillConnected]: ");
            Serial.println(String(connChecker));
            if (connChecker == 1) {
                if (!isFirstBootDone) {
                    isFirstBootDone = true;
                } else {
                    digitalWrite(PIN_BLUE, HIGH);
                    lcd.clear();
                    lcd.setCursor(0, 1);
                    lcd.print("Syncing data...");
                    lcd.setCursor(0, 2);
                    lcd.print("Do not turn off.");

                    for (int node = 0; node < numNodes; node++) {
                        String pillsPath = "/MedRemind/" + String(userUID) + "/" + String(nodeIDs[node]) + "/pillsOnTube";
                        bool status = Database.set<int>(aClient, pillsPath.c_str(), pillsOnTube[node]);
                        if (status) {
                            Serial.println("Set pillsPath is ok");
                        } else {
                            printError(aClient.lastError().code(), aClient.lastError().message());
                        }
                    }

                    getAllSchedule();
                    emptyTube();
                    lcd.clear();
                    digitalWrite(PIN_BLUE, LOW);
                }
            }
        } else {
            printError(aClient.lastError().code(), aClient.lastError().message());
        }

        checkSchedule();
    }
}

void getAllSchedule() {
    // Loop through each node
    for (int node = 0; node < numNodes; node++) {
        String basePath = "/MedRemind/" + String(userUID) + "/" + String(nodeIDs[node]);

        Serial.println("----------------------------");
        Serial.println(basePath);

        Serial.print("schedNumber: ");
        schedNumbers[node] = Database.get<int>(aClient, String(basePath + "/schedNumber").c_str());
        if (aClient.lastError().code() == 0) {
            Serial.println(String(schedNumbers[node]));
        } else {
            printError(aClient.lastError().code(), aClient.lastError().message());
        }

        Serial.print("pillsOnTube: ");
        pillsOnTube[node] = Database.get<int>(aClient, String(basePath + "/pillsOnTube").c_str());
        if (aClient.lastError().code() == 0) {
            Serial.println(String(pillsOnTube[node]));
        } else {
            printError(aClient.lastError().code(), aClient.lastError().message());
        }

        // Loop through each schedule
        for (int i = 0; i < schedNumbers[node]; i++) {
            Serial.println("--- " + String(node) + "  " + String(i));

            Serial.print("triggerDays: ");
            triggerDays[node][i] = Database.get<int>(aClient, String(basePath + "/schedule/" + i + "/repeat").c_str());
            if (aClient.lastError().code() == 0) {
                Serial.println(String(triggerDays[node][i]));
            } else {
                printError(aClient.lastError().code(), aClient.lastError().message());
            }

            Serial.print("triggerTimes: ");
            triggerTimes[node][i] = Database.get<String>(aClient, String(basePath + "/schedule/" + i + "/times").c_str());
            if (aClient.lastError().code() == 0) {
                Serial.println(String(triggerTimes[node][i]));
            } else {
                printError(aClient.lastError().code(), aClient.lastError().message());
            }

            Serial.print("pillQuantities: ");
            pillQuantities[node][i] = Database.get<int>(aClient, String(basePath + "/schedule/" + i + "/pillQuantities").c_str());
            if (aClient.lastError().code() == 0) {
                Serial.println(String(pillQuantities[node][i]));
            } else {
                printError(aClient.lastError().code(), aClient.lastError().message());
            }
        }
    }
}

void emptyTube() {
    /*
     *   int tubeToEmpty
     *
     *   commands/{userUID}/emptyTube/{tubeToEmpty}
     *   tubeToEmpty must be between 0-5, to empty A-E
     *   tubeToEmpty == 9 if done or idle
     */

    String commandPath = "/commands/" + String(userUID) + "/emptyTube";

    int tubeToEmpty = Database.get<int>(aClient, commandPath.c_str());
    if (aClient.lastError().code() == 0) {
        Serial.print(F("[S] commands/emptyTube: "));
        Serial.println(String(tubeToEmpty));
    } else {
        printError(aClient.lastError().code(), aClient.lastError().message());
    }

    if (tubeToEmpty >= 0 && tubeToEmpty < 5) {
        Serial.print("Emptying Tube ");
        Serial.println(tubeToEmpty);

        digitalWrite(PIN_BLUE, HIGH);
        delay(500);
        digitalWrite(PIN_BLUE, LOW);
        delay(500);
        digitalWrite(PIN_BLUE, HIGH);
        delay(500);
        digitalWrite(PIN_BLUE, LOW);
        delay(500);
        digitalWrite(PIN_BLUE, HIGH);

        lcd.setCursor(0, 1);
        lcd.print("Emptying Tube ");
        lcd.print(tubeLabels[tubeToEmpty]);
        delay(1000);
        lcd.setCursor(0, 2);
        lcd.print("Dispensing pill...");

        int ind1 = tubeToEmpty * 2;
        int ind2 = ind1 + 1;

        servos[ind1].attach(pinStoppers[ind1]);
        servos[ind2].attach(pinStoppers[ind2]);
        delay(250);
        servos[ind1].write(0);
        servos[ind2].write(0);
        delay(10000);
        servos[ind1].write(100);
        servos[ind2].write(100);
        delay(10000);

        servos[ind1].detach();
        servos[ind2].detach();

        bool statusA = Database.set<int>(aClient, commandPath.c_str(), 9);
        if (statusA) {
            Serial.println("Set commandPath 9 is ok");
        } else {
            printError(aClient.lastError().code(), aClient.lastError().message());
        }

        String pillsPath = "/MedRemind/" + String(userUID) + "/" + String(tubeToEmpty) + "/pillsOnTube";
        bool statusB = Database.set<int>(aClient, pillsPath.c_str(), 0);
        if (statusB) {
            Serial.println("Set pillsOnTube 0 is ok");
        } else {
            printError(aClient.lastError().code(), aClient.lastError().message());
        }

        digitalWrite(PIN_BLUE, LOW);
        lcd.clear();
    }
}

void cropToHourMinute(const char* fullTime, char* croppedTime) {
    if (strlen(fullTime) >= 5) {
        strncpy(croppedTime, fullTime, 5);
        croppedTime[5] = '\0';
    }
}

void checkSchedule() {
    RTCTime currentTime;
    RTC.getTime(currentTime);

    char dateBuffer[11];
    char timeBuffer[9];
    char croppedTimeBuffer[6];
    getDateNow(dateBuffer, currentTime);
    getTimeNow(timeBuffer, currentTime);
    cropToHourMinute(timeBuffer, croppedTimeBuffer);

    int currentDay = getDayNow(currentTime);

    String ct = String(dateBuffer) + " " + String(croppedTimeBuffer);

    lcd.setCursor(0, 3);
    lcd.print("                    ");
    lcd.setCursor(0, 3);
    lcd.print(ct);

    Serial.print(dateBuffer);
    Serial.print("\t");
    Serial.print(timeBuffer);
    Serial.print("\t");
    Serial.print(croppedTimeBuffer);
    Serial.print("\t");
    Serial.print(currentDay);
    Serial.println();

    for (int node = 0; node < numNodes; node++) {
        for (int i = 0; i < schedNumbers[node]; i++) {
            // Serial.println(String(node) + " " + String(i) + " " + String(triggerDays[node][i]));
            if (triggerDays[node][i] == currentDay || triggerDays[node][i] == 7) {
                Serial.println(triggerTimes[node][i].c_str());
                if (checkTriggerTime(croppedTimeBuffer, triggerTimes[node][i].c_str())) {
                    Serial.print("It's time to take ");
                    Serial.print(pillQuantities[node][i]);
                    Serial.print(" pills at ");
                    Serial.println(triggerTimes[node][i]);

                    dispensePills(node, i);
                }
            }
        }
    }
}

bool checkTriggerTime(const char* currentTime, const char* triggerTime) {
    return strncmp(currentTime, triggerTime, 5) == 0;
}

void dispensePills(int nodeIndex, int scheduleIndex) {
    int quantity = pillQuantities[nodeIndex][scheduleIndex];

    Serial.print("Dispensing ");
    Serial.print(quantity);
    Serial.print(" pills from tube ");
    Serial.println(nodeIDs[nodeIndex]);

    digitalWrite(PIN_BLUE, HIGH);
    delay(500);
    digitalWrite(PIN_BLUE, LOW);
    delay(500);
    digitalWrite(PIN_BLUE, HIGH);
    delay(500);
    digitalWrite(PIN_BLUE, LOW);
    delay(500);
    digitalWrite(PIN_BLUE, HIGH);

    lcd.clear();
    lcd.setCursor(0, 1);
    lcd.print("Time to take meds");
    delay(500);
    lcd.setCursor(0, 2);
    lcd.print("Dispensing pill...");

    for (int i = 1; i <= quantity; i++) {
        if (pillsOnTube[nodeIndex] >= quantity) {
            pillsOnTube[nodeIndex] -= 1;

            int ind1 = nodeIndex * 2;
            int ind2 = ind1 + 1;

            /*
             *   ADJUST SERVO TIMING FOR EACH TUBE
             */

            int servoTiming = 150;
            if (nodeIndex == 4 || nodeIndex == 5) {
                // change servoTiming 150 to your desired value
                // for d and e tubes
                servoTiming = 150;
            }

            servos[ind1].attach(pinStoppers[ind1]);
            servos[ind2].attach(pinStoppers[ind2]);
            delay(250);
            servos[ind1].write(0);
            delay(servoTiming);
            servos[ind1].write(100);
            delay(1000);

            servos[ind2].write(0);
            delay(1000);
            servos[ind2].write(100);
            delay(2000);

            servos[ind1].detach();
            servos[ind2].detach();

            Serial.print("Dispensed: ");
            Serial.println(i);

            lcd.clear();
            while (digitalRead(PIN_BUTTON) == HIGH) {
                lcd.setCursor(3, 1);
                lcd.print("Take your meds");
                lcd.setCursor(9, 2);
                lcd.print("<3");

                tone(PIN_BUZZ, 1000);
                delay(500);
                noTone(PIN_BUZZ);
                delay(500);
            }

            lcd.clear();
            noTone(PIN_BUZZ);
            digitalWrite(PIN_BLUE, LOW);
        } else {
            Serial.print("Not enough pills in tube ");
            Serial.println(nodeIDs[nodeIndex]);

            lcd.clear();
            lcd.setCursor(0, 1);
            lcd.print("Tube empty. Please");
            lcd.setCursor(0, 2);
            lcd.print("refill immediately");
            delay(10000);
            lcd.clear();
        }
    }

    lcd.clear();
    lcd.setCursor(0, 1);
    lcd.print("Remaining pills ");
    lcd.setCursor(0, 2);
    lcd.print("on Tube ");
    lcd.print(tubeLabels[nodeIndex]);
    lcd.print(": ");
    lcd.print(String(pillsOnTube[nodeIndex]));

    String pillsPath = "/MedRemind/" + String(userUID) + "/" + String(nodeIndex) + "/pillsOnTube";
    bool status = Database.set<int>(aClient, pillsPath.c_str(), pillsOnTube[nodeIndex]);
    if (status) {
        Serial.println("Set pillsPath is ok");
    } else {
        printError(aClient.lastError().code(), aClient.lastError().message());
    }

    delay (10000);
    digitalWrite(PIN_BLUE, LOW);
    lcd.clear();
}
