import serial
import firebase_admin
import time
from firebase_admin import credentials
from firebase_admin import db

cred = credentials.Certificate("isw-project-1aaea-firebase-adminsdk-qos9l-411009da82.json")

firebase_admin.initialize_app(cred, {
    "databaseURL": "https://isw-project-1aaea-default-rtdb.europe-west1.firebasedatabase.app/"
})

refValues = db.reference("Values")
refTemperature = refValues.child("Temperature")
refHumidity = refValues.child("Humidity")
refLight = refValues.child("Light")
refSoilMoisture = refValues.child("Soil Moisture")

refDelays = db.reference("Delays")
refPump = refDelays.child("Pump")
refMeasure = refDelays.child("Measure")
refChange = refDelays.child("Change")
refThreshold = refDelays.child("Threshold")
refUnit = refDelays.child("Unit")

refChange.set(True)

arduino_port = "/dev/ttyACM0"  # serial port of arduino
baud = 9600  # arduino runs at 9600 baud
ser = serial.Serial(arduino_port, baud)  # connection to serial port

ser.reset_input_buffer()  # flush data that may already be there

measureValue = 1

def set_delays():
    pumpValue = refPump.get()
    measureValue = refMeasure.get()
    thresholdValue = refThreshold.get()
    measureUnit = refUnit.get()
    if(measureUnit == "minutes"):
        measureValue *= 60
    elif(measureUnit == "hours"):
        measureValue *= (60 * 60)
    elif(measureUnit == "days"):
        measureValue *= (60 * 60 * 24)
    print(thresholdValue)
    refChange.set(False)
    writeData = str(measureValue) + " " + str(pumpValue) + " " + str(thresholdValue)
    ser.write(writeData.encode('utf-8'))

while True:
    print(ser.readline().decode('utf-8').rstrip())
    
    (temperature, humidity, light, soil_moisture) = ser.readline().decode('utf-8').rstrip().split(", ")
    print(temperature, humidity, light, soil_moisture)
    
    
    refValues.set({
        'Temperature': f"{temperature} C",
        'Humidity': f"{humidity}%",
        'Light': f"{light}%",
        'Soil Moisture': f"{soil_moisture}%"
    })
    
    start = time.time()
    end = start
    while(end - start <= measureValue):
        end = time.time()
        if(refChange.get() == True):
            set_delays()
            break