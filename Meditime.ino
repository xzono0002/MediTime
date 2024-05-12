#include <WiFi.h>
#include <RTC.h>
#include <NTPClient.h>
#include <WiFiUdp.h>

#include <Firebase.h>
#include <Firebase_ESP_Client.h>
#include <addons/TokenHelper.h>
#include <addons/RTDBHelper.h>

#include <Servo.h>
#include <Wire.h>
#include <LiquidCrystal.h>
#include <LiquidCrystal_I2C.h>
#include "secrets.h"

RTCTime currentTime;
char ssid[] = WIFI_SSID;
char pass[] = WIFI_PASSWORD;

#define API_KEY ""

#define USER_EMAIL ""
#define USER_PASSWORD ""

#define DATABASE_URL ""
#define DATABASE_SECRET ""
#define BASE_PATH "/MedRemind/"

FirebaseData fbdo;
FirebaseAuth auth;
FirebaseConfig config;

String uid;
char name[20];

Servo servoA1;
Servo servoA2;

Servo servoB1;
Servo servoB2;

Servo servoC1;
Servo servoC2;

Servo servoD1;
Servo servoD2;

Servo servoE1;
Servo servoE2;

LiquidCrystal_I2C lcd(0x27, 20, 4);

const int PIN_BUTTON = 2;
const int PIN_BLUE = 4;
const int PIN_GREEN = 5;
const int PIN_RED = 6;
const int PIN_BUZZ = 7;
const int PIN_STOPPER_A1 = 15; //Tube A
const int PIN_STOPPER_A2 = 14; //Tube A
const int PIN_STOPPER_B1 = 16; //Tube B
const int PIN_STOPPER_B2 = 17; //Tube B
const int PIN_STOPPER_C1 = 10; //Tube C
const int PIN_STOPPER_C2 = 12; //Tube C
const int PIN_STOPPER_D1 = 8;  //Tube D
const int PIN_STOPPER_D2 = 9;  //Tube D
const int PIN_STOPPER_E1 = 13; //Tube E
const int PIN_STOPPER_E2 = 11; //Tube E

void setup() {
  // put your setup code here, to run once:
  Serial.begin(9600);

  pinMode(PIN_BUTTON, INPUT_PULLUP);
  pinMode(PIN_BUZZ, OUTPUT);
  pinMode(PIN_RED, OUTPUT);
  pinMode(PIN_BLUE, OUTPUT);
  pinMode(PIN_GREEN, OUTPUT);

  lcd.begin();
  WiFi.begin(ssid, pass);

  while (WiFi.status() != WL_CONNECTED) {
    lcd.setCursor(0, 1);
    lcd.print("Connecting to WiFi");
    Serial.println("Connecting to WiFi...");
    Serial.println(ssid);
    delay(2000);
    lcd.setCursor(0, 2);
    lcd.print(".");
    delay(1000);
    lcd.print(".");
    delay(1000);
    lcd.print(".");

    Serial.println(WiFi.status());
    delay(10000);
    lcd.clear();
  }

  lcd.setCursor(0, 1);
  lcd.print("WiFi Connected");

  Serial.println("Connected to wifi");
  Serial.print("Connected with IP: ");
  Serial.println(WiFi.localIP());

  if (! RTC.begin()) {
    Serial.println("Couldn't find RTC");
    while (1);
  }

  RTCTime startTime(23, Month::MAY, 2024, 13, 37, 00, DayOfWeek::WEDNESDAY, SaveLight::SAVING_TIME_ACTIVE);

  RTC.setTime(startTime);

  config.api_key = API_KEY;
  auth.user.email = USER_EMAIL;
  auth.user.password = USER_PASSWORD;
  config.database_url = DATABASE_URL;

  Firebase.reconnectWiFi(true);
  fbdo.setResponseSize(4096);

  config.token_status_callback = tokenStatusCallback;
  config.max_token_generation_retry = 5;

  Firebase.begin(&config, &auth);

  Serial.println("Getting User UID");
  while ((auth.token.uid) == "") {
    Serial.print('.');
    delay(1000);
  }
  Serial.print("User UID: ");
  Serial.println(auth.token.uid.c_str());

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
  iniMediTime();
}

void iniMediTime() {
  RTC.getTime(currentTime);

  String path = "/RegisteredUsers/" + String(auth.token.uid.c_str()) + "/username";
  if (Firebase.RTDB.getString(&fbdo, path.c_str())) {
    lcd.setCursor(0, 1);
    lcd.print("Hello ");
    lcd.print(fbdo.stringData());
    delay(10000);
    lcd.clear();
  } else {
    lcd.setCursor(0, 1);
    lcd.print("Error finding name");

    Serial.println();
    Serial.println("Error retrieving name");
    Serial.println("REASON: " + fbdo.errorReason());

    delay(5000);
    lcd.clear();
  }

  empTube(); // Check if user clicked the 'Dispense All' button from the app 
  if (checkTube()){
    fetchAlarm(); // Check if it's time to dispense medicine
  }
  delay(1000);
}

void fetchAlarm(){
  // code to extract time from firebase
  
  dispensePill(); //Dispense fill from tube if it's time
}

void dispensePill(){

  lcd.setCursor(0, 1);
  lcd.print("Time to take meds");

  delay(500);

  lcd.setCursor(0, 2);
  lcd.print("Dispensing pill...");

    if (tube == 'A') {
     // Code to dispense medicine from tube A
     if(checkTube){
      servoA1.write(0);
      delay(150);
      servoA1.write(100);
      delay(1000);

      servoA2.write(0);
      delay(1000);
      servoA2.write(100);
      delay(1000);
      servoA2.write(100);

      lcd.clear();

      // Buzzer will not stop unless user push the button
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
      remainingPills--;
      checkTube(); //check if tube still has pills remaining

      lcd.setCursor(0, 1);
      lcd.print("Remaining pills ");
      lcd.setCursor(0, 2);
      lcd.print("on Tube A: ");
      lcd.print(remainingPills);
      delay(5000);
      lcd.clear();

      digitalWrite(PIN_BLUE, LOW);
    } 

    else if (tube == 'B') {
      // Code to dispense medicine from tube B
      if(checkTube){
      servoB1.write(0);
      delay(150);
      servoB1.write(100);
      delay(1000);

      servoB2.write(0);
      delay(1000);
      servoB2.write(100);
      delay(1000);
      servoB2.write(100);

      lcd.clear();

      // Buzzer will not stop unless user push the button
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
      remainingPills--;
      checkTube();

      lcd.setCursor(0, 1);
      lcd.print("Remaining pills ");
      lcd.setCursor(0, 2);
      lcd.print("on Tube B: ");
      lcd.print(remainingPills);
      delay(5000);
      lcd.clear();

      digitalWrite(PIN_BLUE, LOW);
    } else if (tube == 'C') {
      // Code to dispense medicine from tube C
      if(checkTube){
      servoC1.write(0);
      delay(150);
      servoC1.write(100);
      delay(1000);

      servoC2.write(0);
      delay(1000);
      servoC2.write(100);
      delay(1000);
      servoC2.write(100);

      lcd.clear();

      // Buzzer will not stop unless user push the button
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
      remainingPills--;
      checkTube();

      lcd.setCursor(0, 1);
      lcd.print("Remaining pills ");
      lcd.setCursor(0, 2);
      lcd.print("on Tube C: ");
      lcd.print(remainingPills);
      delay(5000);
      lcd.clear();

      digitalWrite(PIN_BLUE, LOW);
    } 
    else if (tube == 'D') {
      // Code to dispense medicine from tube D
      if(checkTube){
      servoD1.write(0);
      delay(150);
      servoD1.write(100);
      delay(1000);

      servoD2.write(0);
      delay(1000);
      servoD2.write(100);
      delay(1000);
      servoD2.write(100);

      lcd.clear();

      // Buzzer will not stop unless user push the button
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
      remainingPills--;
      checkTube();

      lcd.setCursor(0, 1);
      lcd.print("Remaining pills ");
      lcd.setCursor(0, 2);
      lcd.print("on Tube D: ");
      lcd.print(remainingPills);
      delay(5000);
      lcd.clear();

      digitalWrite(PIN_BLUE, LOW);     
    } 
    else if (tube == 'E') {
      // Code to dispense medicine from tube E  
      if(checkTube){
      servoE1.write(0);
      delay(150);
      servoE1.write(100);
      delay(1000);

      servoE2.write(0);
      delay(1000);
      servoE2.write(100);
      delay(1000);
      servoE2.write(100);

      lcd.clear();

      // Buzzer will not stop unless user push the button
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
      remainingPills--;
      checkTube();

      lcd.setCursor(0, 1);
      lcd.print("Remaining pills ");
      lcd.setCursor(0, 2);
      lcd.print("on Tube E: ");
      lcd.print(remainingPills);
      delay(5000);
      lcd.clear();

      digitalWrite(PIN_BLUE, LOW);    
    }
}

//Check the selected tube for stock
bool checkTube(){
  if(remainingPills == 0){
    lcd.clear();
    lcd.setCursor(0,1);
    lcd.print("Tube empty please");
    lcd.setCursor(0,2);
    lcd.print("refill immediately");
    delay(10000);
    lcd.clear();
    return false;
  } else if(remainingPills <= 2){
      lcd.setCursor(0,1);
      lcd.print("Medicine running low");
      lcd.setCursor(1,2);
      lcd.print("Please refill your");
      lcd.setCursor(8,3);
      lcd.print("tube");
      delay(10000);
      lcd.clear();
      return false;
  } else return true;
}

// Check commands if user want to empty tube
void empTube(){
    if (Firebase.RTDB.getString(&fbdo, "/commands")) {
      String command = fbdo.stringData();
      if (command.startsWith("DISPENSE_TUBE_")) {
          char tube = command.charAt(19); // Extract the tube letter from the command
          
          while(dispenseMed(tube)){
            lcd.setCursor(0, 1);
            lcd.print("Emptying Tube " + tube);
            delay(500);
            lcd.setCursor(0, 2);
            lcd.print("Dispensing pill...");
          }

          // Reset the command after 10 seconds
          Firebase.RTDB.setString(&fbdo, "/commands", "");
      }
    } else {
        lcd.setCursor(0, 1);
        lcd.print("Error fetchCom");
        delay(1000);
    }
}

void dispenseMed(char tube) {
    if (tube == 'A') {
     // Code to dispense medicine from tube A
      servoA1.write(0);
      servoA2.write(0);
      delay(10000);

      servoA1.write(100);
      servoA2.write(100);
      delay(10000);
    } 
    else if (tube == 'B') {
      // Code to dispense medicine from tube B
      servoB1.write(0);
      servoB2.write(0);
      delay(10000);

      servoB1.write(100);
      servoB2.write(100);
      delay(10000);
    } else if (tube == 'C') {
      // Code to dispense medicine from tube C
      servoC1.write(0);
      servoC2.write(0);
      delay(10000);

      servoC1.write(100);
      servoC2.write(100);
      delay(10000);
    } 
    else if (tube == 'D') {
      // Code to dispense medicine from tube D      
      servoD1.write(0);
      servoD2.write(0);
      delay(10000);

      servoD1.write(100);
      servoD2.write(100);
      delay(10000);
    } 
    else if (tube == 'E') {
      // Code to dispense medicine from tube E      
      servoE1.write(0);
      servoE2.write(0);
      delay(10000);

      servoE1.write(100);
      servoE2.write(100);
      delay(10000);
    }
}

void displayTime(){
  lcd.clear();
  lcd.setCursor(0,1);  

  // Get current time
  int month = Month2int(currentTime.getMonth());
  int day = currentTime.getDayOfMonth();
  int hours = currentTime.getHour();
  int minutes = currentTime.getMinutes();
  String ampm;

  // Code to follow date and time format in 01/01/2001 01:01 AM
  if (month < 10) {
    lcd.print("0");
  }
  lcd.print(month);
  lcd.print('/');

  if (day < 10) {
    lcd.print("0");
  }
  lcd.print(day);
  lcd.print('/');
  lcd.print(currentTime.getYear(), DEC);

  lcd.print(" ");

  if(hours < 12){
    ampm = "AM";
    if (hours == 0) {
    hours = 12;  // midnight
    }
  } else{
    ampm = "PM";
    hours = hours - 12;
  }

  if (hours < 10) {
    lcd.print("0");
  }
  
  lcd.print(hours);
  lcd.print(':');

  if (minutes < 10) {
    lcd.print("0");
  }
  lcd.print(minutes);
  lcd.print(" ");
  lcd.print(ampm);

  delay(10000);
  lcd.clear();
}