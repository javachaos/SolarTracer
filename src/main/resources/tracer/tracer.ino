/*
 * An interface to the Tracer solar regulator.
 * Communicating in a way similar to the MT-5 display
 */
#include <SoftwareSerial.h>

unsigned int speed = 1000; // Default update speed.
SoftwareSerial myserial(10, 11); // RX, TX

uint8_t start[] = { 0xAA, 0x55, 0xAA, 0x55, 0xAA, 0x55,
                    0xEB, 0x90, 0xEB, 0x90, 0xEB, 0x90 };
uint8_t id = 0x16;
uint8_t cmd[] = { 0xA0, 0x00, 0xB1, 0xA7, 0x7F };
uint8_t buff[128];
uint8_t input[4];
char sep = ':';

void setup() {
  Serial.begin(57600);
  myserial.begin(9600);
}

// Convert two bytes to a float
float to_float(uint8_t* buffer, int offset) {
  unsigned short full = buffer[offset+1] << 8 | buff[offset];
  return full / 100.0;
}

// Read sleep time from client application.
int getSleepTime() {
  int read = 0;
  // Read the next 4 bytes from Serial into input buffer.
  for (int j = 0; j < 4; j++) {
    if (Serial.available() > 0) {
      input[read] = Serial.read();
      read++;
    }
  }

  read = (input[0] << 24) | (input[1] << 16) | (input[2] << 8) | input[3];
  if (read > 86400000 || read < 1000) { // 1 day in milliseconds or 1 second.
    return speed;
  }
  // Convert it into an integer and return it.
  return read;
}

void loop() {

  myserial.write(start, sizeof(start));
  myserial.write(id);
  myserial.write(cmd, sizeof(cmd));

  int read = 0;

  for (int i = 0; i < 255; i++){
    if (myserial.available()) {
      buff[read] = myserial.read();
      read++;
    }
  }
  
  // 9 Battery voltage.
  float battery = to_float(buff, 9);
  Serial.print(battery);
  Serial.print(sep);

  // 11 PV Voltage
  float pv = to_float(buff, 11);
  Serial.print(pv);
  Serial.print(sep);

  // 15 Load Current
  float load_current = to_float(buff, 15);
  Serial.print(load_current);
  Serial.print(sep);

  // 17 Over discharge
  float over_discharge = to_float(buff, 17);
  Serial.print(over_discharge);
  Serial.print(sep);

  // 19 Battery Max
  float battery_max = to_float(buff, 19);
  Serial.print(battery_max);
  Serial.print(sep);

  // 27 Battery full yes/no
  uint8_t full = buff[27];
  Serial.print(full);
  Serial.print(sep);
  
  // 28 Charging
  uint8_t charging = buff[28];
  Serial.print(charging);
  Serial.print(sep);

  // 29 Battery Temp
  int8_t battery_temp = buff[29] - 30;
  Serial.print(battery_temp);
  Serial.print(sep);

  // 30 Charge Current
  float charge_current = to_float(buff, 30);
  Serial.print(charge_current);
  Serial.print(sep);

  // 21 Load on / off
  uint8_t load_onoff = buff[21];
  Serial.print(load_onoff);
  Serial.println();
  delay(getSleepTime());
}

