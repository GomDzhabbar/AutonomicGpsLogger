#include <SD.h>
#include <stdio.h>
#include <String>
#include <Wire.h> 
#include <LiquidCrystal_I2C.h>
#include <SoftwareSerial.h>
#include <TinyGPS.h>
#define DHT11_PIN A1

TinyGPS gps;
SoftwareSerial ss(5, 3);
static void print_float(float val, float invalid, int len, int prec);

boolean menu = false, select = false;
int mode = 0, mode_menu = 0;

int bee = 11;

LiquidCrystal_I2C lcd(0x27, 16, 2);

int lcd_key     = 0;
int adc_key_in  = 0;
int countDot = 1;
int delayValue = 1000;

File dataFile;

#define btnRIGHT  0
#define btnUP     1
#define btnDOWN   2
#define btnLEFT   3
#define btnSELECT 4
#define btnNONE   5

boolean displayOn = true;
 
int read_LCD_buttons(){
    adc_key_in = analogRead(0); 

    if (adc_key_in > 1000) return btnNONE; 

    // For V1.1 us this threshold
    if (adc_key_in < 50)   return btnRIGHT;  
    if (adc_key_in < 250)  return btnUP; 
    if (adc_key_in < 450)  return btnDOWN; 
    if (adc_key_in < 650)  return btnLEFT; 
    if (adc_key_in < 850)  return btnSELECT;  

    return btnNONE;
}

void setup(){

	Serial.begin(9600);
	ss.begin(9600);

  Serial.println("start");

	// // pinMode(bee, OUTPUT);
	pinMode(0, INPUT);
	pinMode(1, INPUT);
	pinMode(12, OUTPUT);

	lcd.begin();
	lcd.backlight();
	lcd.setCursor(0,0); 

	if (!SD.begin(4))
	lcd.println("Card failed  ");
	else
	lcd.println("card init  ");
	delay(3000);
}

void loop(){
  char sz[32];

  if(displayOn)
    lcd.clear();

  float flat, flon;
  unsigned long age, date, time, chars = 0;
  gps.f_get_position(&flat, &flon, &age);

  if(analogRead(3) < 500){
    displayOn = false;
    Serial.println(analogRead(3));
  }
  else {
    displayOn = true;
    Serial.println(analogRead(3));
  }



  if (flat == TinyGPS::GPS_INVALID_F_ANGLE){
    if(displayOn)
  	 lcd.print("gps error");
     delayValue = 1000;
  } else {
    delayValue = 60000;
    int year;
    byte month, day, hour, minute, second, hundredths;
    unsigned long age;
    File dataFile;
    gps.crack_datetime(&year, &month, &day, &hour, &minute, &second, &hundredths, &age);

	  // print_date(gps);
    // формируем название файла по дате (новый день новый файл)
    

     if (age == TinyGPS::GPS_INVALID_AGE) {
        Serial.print("********** ******** ");
      } else {

        sprintf(sz, "%04d%02d%02d.txt",year, month, day);

        dataFile = SD.open(sz, FILE_WRITE);
      }

    if (dataFile) {
//----------------------------------------------------------------------------------------------------------------------------------------
      sprintf(sz, "%04d-%02d-%02dT%02d:%02d:%02dZ",year, month, day, hour+3, minute, second);
     
      // Serial.print(sz);
      dataFile.print("<when>");
      dataFile.print(sz);
      dataFile.println("</when>");

      sprintf(sz, "%02d:%02d",
            hour+3, minute);

      if(displayOn){
        lcd.setCursor(10,0); 
        lcd.print(sz);
      }

      sprintf(sz, "%d", countDot);
      if(displayOn) {
        lcd.setCursor(10,1); 
        lcd.print(sz);
      	lcd.setCursor(0,0); 
      }
 //----------------------------------------------------------------------------------------------------------------------------------------
			print_float(flon, TinyGPS::GPS_INVALID_F_ANGLE, 10, 6);

      if(displayOn) 
			 lcd.print(flon,4);

			dataFile.print("<gx:coord>");
      dataFile.print(flon,5);
      dataFile.print(" ");

      if(displayOn)
			 lcd.setCursor(0,1); 

			print_float(flat, TinyGPS::GPS_INVALID_F_ANGLE, 11, 6);

      if(displayOn)
			 lcd.print(flat,4);

			dataFile.print(flat,5);
      dataFile.println("</gx:coord>\n");

      countDot++;

      dataFile.close();
    } else {
      // Serial.println("error opening gps4.txt");
      if(displayOn) {
        lcd.setCursor(0,0);
        delayValue = 1000;
        lcd.println("Error card  ");
      }
    } 

	  // Serial.println();
  }	
  
  delay(delayValue);
}

static void smartdelay(unsigned long ms)
{
  unsigned long start = millis();
  do 
  {
    while (ss.available())
      gps.encode(ss.read());
  } while (millis() - start < ms);
}

static void print_float(float val, float invalid, int len, int prec)
{
  if (val == invalid)
  {
    while (len-- > 1)
      Serial.print('*');
    Serial.print(' ');
  }
  else
  {
    // Serial.print(val, prec);
    int vi = abs((int)val);
    int flen = prec + (val < 0.0 ? 2 : 1); // . and -
    flen += vi >= 1000 ? 4 : vi >= 100 ? 3 : vi >= 10 ? 2 : 1;
    for (int i=flen; i<len; ++i)
      Serial.print(' ');
  }
  smartdelay(0);
}

static void print_date(TinyGPS &gps)
{
  int year;
  byte month, day, hour, minute, second, hundredths;
  unsigned long age;
  gps.crack_datetime(&year, &month, &day, &hour, &minute, &second, &hundredths, &age);
  if (age == TinyGPS::GPS_INVALID_AGE)
    Serial.print("********** ******** ");
  else
  {
    char sz[32];
    sprintf(sz, "%02d/%02d/%02d %02d:%02d:%02d ",
        month, day, year, hour, minute, second);
    // Serial.print(sz);
  }
  // print_int(age, TinyGPS::GPS_INVALID_AGE, 5);
  smartdelay(0);
}

static void print_int(unsigned long val, unsigned long invalid, int len)
{
  char sz[32];
  if (val == invalid)
    strcpy(sz, "*******");
  else
    sprintf(sz, "%ld", val);
  sz[len] = 0;
  for (int i=strlen(sz); i<len; ++i)
    sz[i] = ' ';
  if (len > 0) 
    sz[len-1] = ' ';
  // Serial.print(sz);
  smartdelay(0);
}
/*
1) добавить звуковой и световой сигнал ошибки от gps 
2) добавить на отображение время. на дисплей, и в трек.
*/