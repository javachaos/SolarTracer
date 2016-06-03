/*
 * An interface to the Tracer solar regulator.
 * Communicating in a way similar to the MT-5 display
 */
#include <SoftwareSerial.h>

unsigned int speed = 1000; // Default update speed.
SoftwareSerial mppt_serial(10, 11); // RX, TX

// DATA SYNCHRONIZATION BYTES
uint8_t start[] = { 0xAA, 0x55, 0xAA, 0x55, 0xAA, 0x55,
                    0xEB, 0x90, 0xEB, 0x90, 0xEB, 0x90 };
uint8_t input[4];
char sep = ':';

String inputString = "";         // a string to hold incoming data
boolean stringComplete = false;  // whether the string is complete

void setup() {
  Serial.begin(57600);
  while (!Serial) {
    ; // wait for serial port to connect. Needed for native USB port only
  }
  // reserve 200 bytes for the inputString:
  inputString.reserve(200);
  mppt_serial.begin(9600);
}

// Tested works OK
uint16_t crc(uint8_t *CRC_Buff, uint8_t crc_len) {
	uint8_t crc_i, crc_j, r1, r2, r3, r4;
	uint16_t crc_result;
	r1 =*CRC_Buff;
	CRC_Buff++;
	r2=*CRC_Buff;
	CRC_Buff++;
	for (crc_i = 0; crc_i < crc_len -2; crc_i++) {
		r3 =* CRC_Buff;
		CRC_Buff++;
		for (crc_j=0; crc_j < 8; crc_j++) {
			r4 = r1;
			r1 = (r1<<1);
			if ((r2 & 0x80) != 0) {
				r1++;
			}
			r2 = r2<<1;
			if((r3 & 0x80) != 0) {
				r2++;
			}
			r3 = r3<<1;
			if ((r4 & 0x80) != 0) {
				r1 = r1^0x10;
				r2 = r2^0x41;
			}
		}
	}
	crc_result = r1;
	crc_result = crc_result << 8 | r2;
	return crc_result;
}

// Convert two bytes to a float. OK
float to_float(uint8_t* buffer, int offset) {
  unsigned short full = buffer[offset+1] << 8 | buffer[offset];
  return full / 100.0;
}

// Calc and add crc bytes to data.
uint8_t* calc_and_addcrc(uint8_t* data) {
  uint16_t crc_d = crc(data, data[2] + 5);
  data[data[2] + 3] = crc_d >> 8;
  data[data[2] + 4] = crc_d & 0xFF;
  return data;
}

// Read sleep time from client application.
void updateSleepTime(String sleepSpeed) {
  if (sleepSpeed.length() != 5) {//4bytes plus 1 newline
    return;
  }

  char* s = sleepSpeed.c_str();
  for (int j = 0; j <= 3; j++) {
      input[j] = s[j];
  }

  speed = (input[0] << 24) | (input[1] << 16) | (input[2] << 8) | input[3];
  if (read > 86400000 || read < 1000) { // 1 day in milliseconds or 1 second.
    speed = 1000;
  }
}

void manualControlCmd(bool load_onoff) {
  mppt_serial.write(start, sizeof(start));
  uint8_t mcc_data[] = { 0x16,       //DEVICE ID BYTE
	                     0xAA,       //COMMAND BYTE
	                     0x01,       //DATA LENGTH
						 0x00,
	                     0x00, 0x00, //CRC CODE
	                     0x7F };     //END BYTE
  if (load_onoff) {
	  mcc_data[3] = 1;
  } else {
	  mcc_data[3] = 0;
  }
  //Calculate and add CRC bytes.
  uint8_t* d = calc_and_addcrc(mcc_data);
  mppt_serial.write(d, sizeof(d));
}

void printAllData() {
  uint8_t data[] = { 0x16,       //DEVICE ID BYTE
                     0xA0,       //COMMAND BYTE
                     0x00,       //DATA LENGTH
                     0xB1, 0xA7, //CRC CODE
                     0x7F };     //END BYTE

  uint8_t buff[128];
  mppt_serial.write(start, sizeof(start));
  mppt_serial.write(data, sizeof(data));
    
  int read = 0;

  for (int i = 0; i < 255; i++) {
    if (mppt_serial.available()) {
      buff[read] = mppt_serial.read();
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
}

void loop() {
  printAllData();
  Serial.println();
  if (stringComplete) {
    if (inputString == "LON") {
      manualControlCmd(true);
    } else if (inputString == "LOFF") {
      manualControlCmd(false);
    } else {
      updateSleepTime(inputString);
    }
    // clear the string:
    inputString = "";
    stringComplete = false;
  }
  delay(speed);
}

void serialEvent() {
  while (Serial.available()) {
    // get the new byte:
    char inChar = (char)Serial.read();
    // if the incoming character is a newline, set a flag
    // so the main loop can do something about it:
    if (inChar != '\n') {
      // add it to the inputString:
      inputString += inChar;
    } else if (inChar == '\n') {
      stringComplete = true;
    }
  }
}
