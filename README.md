# Color Namer

This project is an Android application that tells the name of the color pointed by the camera.  
This app is intended to be used by colorblind people to help them to know the color of things. Therefore the available color names are the following: Red, Green, Blue, Purple, Pink, Yellow, Cyan, Orange, Brown, Gray, Black, and White. As a colorblind, I think these colors are enough to know the color of an object. We don't need to know the exact color name, like Mole or Lavender, which don't help colorblind people.  
To do so, I use the HSV values of the color to read its hue and then extract the color.  
The app is available in English and French (adjusting an in-app setting).

## Download and install the app
Check the release of the app [here](https://github.com/borisbordeaux/ColorNamer/releases) and download the apk file.  
Then you need to install it directly using your phone.

## How to build

Clone this repo and add a `local.properties` file at the root folder of the Android app:  
```bash
git clone https://github.com/borisbordeaux/ColorNamer.git
cd ColorNamer
printf "sdk.dir=/path/to/Android/Sdk\n" > local.properties
printf "signings.store.path=/path/to/keystore/file.jks\n" >> local.properties
printf "signings.store.password=keystore_password\n" >> local.properties
printf "signings.key.alias=key_name\n" >> local.properties
printf "signings.key.password=key_password\n" >> local.properties
```
You should create a store and a key to sign the app in release mode.
Then you should be able to open the folder containing this file in Android Studio.

## Pipeline

### Step 1

I use the camera to get the picture.

### Step 2

I read some pixels in the center of the image to get the mean color of the pointed thing.

### Step 3

I convert the RGB values of the color to HSV values. More details in wikipedia [here](https://en.wikipedia.org/wiki/HSL_and_HSV).

### Step 4

I determine the color depending on the Hue, Saturation, and Value values.  
The Hue tells the color.  
The Saturation tells how much the color is present.  
The Value tells how much the color is dark or light.  
Having this information, I classify the colors with the following rules:  
Hue < 15 or >= 346 = Red  
15 <= Hue < 40 = Brown if saturation < 0.75 else Orange  
40 <= Hue < 74 = Yellow  
74 <= Hue < 155 = Green  
155 <= Hue < 186 = Cyan  
186 <= Hue < 278 = Blue  
278 <= Hue < 330 = Purple  
330 <= Hue < 346 = Pink  
Then I use Saturation and Value to know if it is gray, black or white:  
Value < 0.18 = Black  
Value > 0.18 and Saturation < 0.1 and Value < 0.85 = Gray  
Value > 0.18 and Saturation < 0.1 and Value >= 0.85 = White  

These rules are handmade, they may not be precise enough depending on the situation.
