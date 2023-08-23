#include <FirebaseESP8266.h>

#include <ESP8266WiFi.h>
#include <NTPClient.h>
#include <WiFiUdp.h>

#define FIREBASE_AUTH "k4WYtm84hLKZMxhvNVF0lD6Untz9Nr9aY82pGpox"
#define FIREBASE_HOST "https://du-an-75630-default-rtdb.asia-southeast1.firebasedatabase.app/"
#define WIFI_SSID "Hai Uyen"
#define WIFI_PASSWORD "04082001"
#include <ESP8266WiFi.h>
//
#include <DHT.h>

#define DHTPIN 5     // chân GPIO mà bạn đã kết nối với chân DATA của DHT11
#define DHTTYPE DHT11   // Loại cảm biến DHT

DHT dht(DHTPIN, DHTTYPE);
//
const int LED = 4;
// Firebase setup
FirebaseData fbdo;
FirebaseData fbdo_temp;
FirebaseData fbdo_hump;
FirebaseData fbdo_motor;
FirebaseData fbdo_hour;
FirebaseData fbdo_minute;
FirebaseJson json;
WiFiUDP ntpUDP;
NTPClient timeClient(ntpUDP, "vn.pool.ntp.org");
/// Data setup
int i = 0;
int mode = 0; // 0 - auto -- 1 manual
int minute;
int hour;
int motor;
//
int value, real_value;
void setup() {
  Serial.begin(9600);
  pinMode(LED, OUTPUT);
  digitalWrite(LED, LOW);
  dht.begin();

  WiFi.begin(WIFI_SSID, WIFI_PASSWORD);
  Serial.print("connecting");
  while (WiFi.status() != WL_CONNECTED) {
    Serial.print(".");
    delay(500);
  }
  Serial.println();
  Serial.print("connected: ");
  Serial.println(WiFi.localIP());
  //firebase setup
  Firebase.begin(FIREBASE_HOST, FIREBASE_AUTH);
  //
  timeClient.begin();
  timeClient.setTimeOffset(+7 * 60 * 60);

  while (!timeClient.update()) {
    timeClient.forceUpdate();
  }
}
void loop() {
  timeClient.update();
  delay(250);
  float newHump = dht.readHumidity(); // Đọc độ ẩm từ cảm biến
  float newTemp = dht.readTemperature(); // Đọc nhiệt độ từ cảm biến


  // gửi dữ liệu lên firebase
  Firebase.setFloat(fbdo, "/hump", newHump);
  Firebase.setFloat(fbdo, "/temp", newTemp);

// lấy dữ liệu từ firebase
  Firebase.getInt(fbdo, "/mode");
  Firebase.getInt(fbdo_hour, "/time/hour");
  Firebase.getInt(fbdo_minute, "/time/minute");
  Firebase.getInt(fbdo_motor, "/motor");

  mode = fbdo.to<int>();
  hour = fbdo_hour.to<int>();
  minute = fbdo_minute.to<int>();
  motor = fbdo_motor.to<int>();

  Serial.print("\n -------------Clear----------------- ");
  Serial.print("\n mode: "); Serial.print(mode);
  Serial.print("\n Server time | hour: "); Serial.print(timeClient.getHours());
  Serial.print("-- minute: "); Serial.print(timeClient.getMinutes());


  //
  Serial.print("\n Hour: "); Serial.print(hour); Serial.print("-- Minute: "); Serial.print(minute);
  // hen gio
  if ((hour == timeClient.getHours()) && (minute == timeClient.getMinutes()))
  {
    digitalWrite(LED, HIGH);
    Serial.print("\n LED on ---- Time up");
  }
  if ((hour == timeClient.getHours()) && (minute == timeClient.getMinutes() + 5))
  {
    digitalWrite(LED, LOW);
    Serial.print("\n LED OFF -- Turn");
  }

  // che do manual -- bat motor
  if (mode == 0 && motor == 1) {
    digitalWrite(LED, HIGH);
    Serial.print("\n LED on");
  }
  else if (mode == 0 && motor == 0) {
    digitalWrite(LED, LOW);
    Serial.print("\n LED off");
  }


  // hiển thị lên serial để kiểm tra
  Serial.print("\n mode: "); Serial.print(mode);
  Serial.print("\n hour: "); Serial.print(hour);
  Serial.print("\n minute: "); Serial.print(minute);
  Serial.print("\n temp: "); Serial.print(newTemp);
  Serial.print("\n hump: "); Serial.print(newHump);

}
