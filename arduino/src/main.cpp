/*
 * Arthur: Rio
 * Date: 2017/02/24
 */

#include <Arduino.h>
#include "AFMotor.h"

typedef enum {
    NONE,
    MOTOR,
    SERVO
} ParseType;

struct {
    ParseType type;
    int mSpeed;
    int mCmd;
    int sAngle;
    int index;
} parser;

AF_DCMotor motors[] = {
    AF_DCMotor(1),
    AF_DCMotor(2),
    AF_DCMotor(3),
    AF_DCMotor(4)};
int motorNum = 4;

void run() {
    if(parser.index < 0) return;
    if(parser.type == NONE) return;

    switch(parser.type) {
        case MOTOR:
            if(parser.index < motorNum) {
                AF_DCMotor motor = motors[parser.index];
                if(parser.mCmd >= 0)
                    motor.run(parser.mCmd);
                if(parser.mSpeed >= 0)
                    motor.setSpeed(parser.mSpeed);
            }
            break;
        case SERVO:
            // Ignore servo for now
            break;
        case NONE:
            break;
    }
}

void setup() {
    Serial.begin(9600);
    parser.type = NONE;
    parser.mSpeed = parser.mCmd = parser.sAngle = parser.index = -1;
}

void loop() {
    if(Serial.available() > 0) {
        int val = Serial.read();
        Serial.write(val); // For debug

        if(val == '>') {
            parser.type = NONE;
            parser.mSpeed = parser.mCmd = parser.sAngle = parser.index = -1;
        } else if(val == '<') {
            run();
        } else {
            if(parser.type == NONE) {
                switch(val) {
                    case 'M':
                        parser.type = MOTOR;
                        break;
                    case 'S':
                        parser.type = SERVO;
                        break;
                }
            } else {
                if(parser.index < 0) {
                    parser.index = val;
                } else {
                    switch(parser.type) {
                        case MOTOR:
                            if(parser.mSpeed < 0)
                                parser.mSpeed = val & 0xff;
                            else if(parser.mCmd < 0)
                                parser.mCmd = val;
                            break;
                        case SERVO:
                            if(parser.sAngle < 0)
                                parser.sAngle = val;
                            break;
                        case NONE:
                            break;
                    }
                }
            }
        }
    }
}
