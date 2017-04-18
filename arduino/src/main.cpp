#include <Arduino.h>
#include <Servo.h> 

int pinLF=14;
int pinLB=15;
int pinRF=16;
int pinRB=17;

void setup() {
    Serial.begin(9600);
    // 定義馬達輸出腳位 
    pinMode(pinLB,OUTPUT);
    pinMode(pinLF,OUTPUT);
    pinMode(pinRB,OUTPUT);
    pinMode(pinRF,OUTPUT);

}

void loop() {
    if(Serial.available()) {
        int val = Serial.read();
        if(val == 'w') { // forward
            digitalWrite(pinRF,HIGH);
            digitalWrite(pinRB,LOW);
            digitalWrite(pinLF,HIGH);
            digitalWrite(pinLB,LOW);
        }
        if(val == 's') { // back
            digitalWrite(pinRF,LOW);
            digitalWrite(pinRB,HIGH);
            digitalWrite(pinLF,LOW);
            digitalWrite(pinLB,HIGH);
        }
        if(val == 'a') { // left
            digitalWrite(pinRF,HIGH);
            digitalWrite(pinRB,LOW);
            digitalWrite(pinLF,LOW);
            digitalWrite(pinLB,HIGH);
        }
        if(val == 'd') { // right
            digitalWrite(pinRF,LOW);
            digitalWrite(pinRB,HIGH);
            digitalWrite(pinLF,HIGH);
            digitalWrite(pinLB,LOW);
        }
        if(val == ' ') { // stop
            digitalWrite(pinRF,LOW);
            digitalWrite(pinRB,LOW);
            digitalWrite(pinLF,LOW);
            digitalWrite(pinLB,LOW);
        }
        Serial.write(val);
    }
}
