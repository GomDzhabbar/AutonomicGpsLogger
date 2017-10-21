//AGLgd.ino
#include <SoftwareSerial.h>
#include <TinyGPS.h>
#include <SD.h>
#include <OLED_I2C.h>

TinyGPS gps;
SoftwareSerial ss(5, 3);

OLED  myOLED(SDA, SCL, 8);

extern uint8_t SmallFont[];

static void print_float(float val, float invalid, int len, int prec);

File myFile;

void setup() {
    ss.begin(9600);

    myOLED.begin();
    myOLED.setFont(SmallFont);

    myOLED.print("Card faile12222d", CENTER, 32); 
    myOLED.update();
    delay(1000);

    if (!SD.begin(4)){
        myOLED.print("Card failed", CENTER, 32); 
        myOLED.update();
        delay(1000);
    } else { 
        myOLED.print("Card init", CENTER, 32);
        myOLED.update();
        delay(1000);
    }

     // myFile = SD.open("test.txt", FILE_WRITE);
}

void loop() {
    float flat, flon;
    unsigned long age;

  if (myFile) {
    myOLED.clrScr();
    myOLED.print("Writing to test.txt...", LEFT, 0);
    myOLED.update();
    delay(1000);

    // myFile.println("testing 1, 2, 3.");
    // close the file:
    // myFile.close();

    myOLED.clrScr();
    myOLED.print("Done", LEFT, 0);
    myOLED.update();
    delay(1000);
  } else {
    // if the file didn't open, print an error:
    myOLED.clrScr();
    myOLED.print("error opening test.txt", LEFT, 0);
    myOLED.update();
    delay(1000);
  }
  
  // re-open the file for reading:
  myFile = SD.open("test.txt");
  if (myFile) {
    myOLED.clrScr();
    myOLED.print("test.txt:", LEFT, 0);
    myOLED.update();
    delay(1000);
    
    // read from the file until there's nothing else in it:
    while (myFile.available()) {
        myOLED.clrScr();
        myOLED.printNumI(myFile.read(), LEFT, 32);
        myOLED.update();
        delay(1000);
    }
    // close the file:
    myFile.close();
  } else {
    // if the file didn't open, print an error:
    myOLED.clrScr();
    myOLED.print("error opening txt", LEFT, 32);
    myOLED.update();
    delay(1000);
  }

    // gps.f_get_position(&flat, &flon, &age);

    // myOLED.clrScr();

    // print_float(flat, TinyGPS::GPS_INVALID_F_ANGLE, 10, 6);

    delay(1000);
}

static void print_float(float val, float invalid, int len, int prec)
{
    if (val == invalid)
    {
        myOLED.print("*****", CENTER, 32); 
        myOLED.update();
    }
    else
    {
        myOLED.print("Salut!!", CENTER, 32); 
        myOLED.update();
    }
    delay(100);
}
