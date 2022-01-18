#include <dht.h>

dht DHT;

int soil_moisture_pin = A0; // analog pin for the soil moisture sensor
int light_pin = A1;         // analog pin for photoresistor
int pump_pin = 3;           // digital pin for relay
int dth_pin = 2;            // digital pin for temperature and humidity sensor
int soil_trigger_pin = 4;   // digital pin for triggering the soil moisture sensor

int threshold = 30;         // threshold value to trigger pump

int soil_moisture;          // value of the soil moisture
int light;                  // value for light intensity
int temperature;            // value for temperature
int humidity;               // value for humidity

long measureDelay = 1000;   // delay for next loop iteration
int pumpRunTime = 1000;     // how long the pump will work

void setup() {
  Serial.begin(9600);
  pinMode(soil_moisture_pin, INPUT);
  pinMode(light_pin, INPUT);
  pinMode(pump_pin, OUTPUT);
  pinMode(dth_pin, INPUT);
  pinMode(soil_trigger_pin, OUTPUT);
  digitalWrite(soil_trigger_pin, LOW);
  delay(1000);
}

void loop() {
  Serial.print("Delays ");
  Serial.print(measureDelay);
  Serial.print(", ");
  Serial.println(pumpRunTime);
  
  trigger_dht_sensor();
  trigger_light_sensor();
  trigger_soil_moisture_sensor();

  delay(1000); // wait 1 s

  Serial.print(temperature);
  Serial.print(", ");
  Serial.print(humidity);
  Serial.print(", ");
  Serial.print(light);
  Serial.print(", ");
  Serial.println(soil_moisture);

  if(soil_moisture < threshold){ // if soil is dry pump water
    trigger_pump();
  }
  else{
    digitalWrite(pump_pin, LOW);
    delay(1000); //wait 1 s
  }
  
  myDelay(measureDelay);
}

void myDelay(int del){
  unsigned long myPrevMillis = millis();
  unsigned long myCurrentMillis = myPrevMillis;
  while (myCurrentMillis - myPrevMillis <= del) {
    myCurrentMillis = millis();
    
    int serialAvailable = Serial.available();
    //Serial.println(serialAvailable);
  
    if(serialAvailable){
      //Serial.println("New Delays");

      long measureValue = Serial.parseInt();
      int pumpValue = Serial.parseInt();
      threshold = Serial.parseInt();
      Serial.read(); // read the last character

      measureDelay = 1000 * measureValue;
      pumpRunTime = 1000 * pumpValue;

      break;
    }
  }
}

void trigger_soil_moisture_sensor(){
  // activate sensor
  digitalWrite(soil_trigger_pin, HIGH);
  delay(100);
  
  soil_moisture = analogRead(soil_moisture_pin);          // get value from the soil moisture sensor
  soil_moisture = constrain(soil_moisture, 400, 1023);    // set values to be between 400 and 1023
  soil_moisture = map(soil_moisture, 400, 1023, 100, 0);  // 400 -> 100%, 1023 -> 0%

  // deactivate sensor
  digitalWrite(soil_trigger_pin, LOW);

  delay(1000); // wait 1 s
}

void trigger_light_sensor(){
  light = analogRead(light_pin);        // get value from photoresistor (between 0 and 1023)
  light = map(light, 0, 1023, 0, 100);  // 1023 -> 100%, 0 -> 0%

  delay(1000); // wait 1 s
}

void trigger_pump(){
  digitalWrite(pump_pin, HIGH);   // activate relay
  delay(pumpRunTime);             // run pump for some time
  digitalWrite(pump_pin, LOW);    // deactivate relay
  
  delay(1000); //wait 1 s
}

void trigger_dht_sensor(){
  DHT.read11(dth_pin);

  // get values from DHT sensor
  humidity = DHT.humidity;
  temperature = DHT.temperature;

  delay(1000); //wait 1 s
}
