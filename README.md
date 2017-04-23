# ArduinoCar
Controlling an Arduino car using android and wifi direct.

## Protocol
The phone and arduino communicate with this protocol:
```
>TypeIndexDatas<
```
Where:
- \> Indicates the start of a command
- Type can either be `M` (motor) or `S` (servo) [1 byte]
- Index is to specify which motor/servo to control [1 byte]
- Datas are speed and run mode for motor, angle for servo [multiple bytes]
- \< Indicated the end of command  

Example:
```java
    // Set the motor at index 0 to speed 200 and run forward
    byte[] cmd = {'>', 'M', 0, 200, 1, '<'};
```
