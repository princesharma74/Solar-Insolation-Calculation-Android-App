## Team Name
The Cyborgs

## Organization Name
Indian Space Research Organization (ISRO)

## PROBLEM STATEMENT
Generating local sky horizon has important applications for analysis of solar energy potential in an urban setting. Develop a mobile application for automatically detecting sky pixels in a photograph. The application should generate a mask image consisting of sky pixels marked in white colour in the image and other pixels marked in black colour. Further, using information about camera optics, the application should give the angle of elevation of the lowest sky pixel for all pixel columns in the mask image.

## Data Set link
[Data Set Link](https://vedas.sac.gov.in/vcms/en/sih2020.html)

## PS NO.
NM381

## CATEGORY
Software

## TEAM LEADER NAME
Prince Sharma

## COLLEGE CODE
C-6380

## INTRODUCTION
SOLUTION DESCRIPTION
The purpose of this project is to devise a software solution to ease the analysis and calculation of the best solar site, which is significantly affected by surrounding obstacles for the sun rays like buildings, trees, etc. We are aiming to create a first-of-its-own-kind smartphone application with which ordinary users as well as Solar Energy Providers can calculate the actual solar potential at a particular location, optimal panel position, land area needed, and many other utilities.

Steps:
1. User will be guided using markers to record a video of the sky horizon around the location through real-time sky segmentation of frames.
2. Each frame embedded with the corresponding orientational parameters (i.e., azimuth, pitch, and roll of the smartphone) will later be used to find out the Angle of Elevation (AoE) of the lowest sky pixels.
3. Effective Solar Insolation (Instant/daily/monthly/yearly) of the location will be calculated with the help of a graph created between Elevation and Azimuth.

Key Features:
- Calculates energy yield (KWh)
- Comparison of Two or More Sites
- Save Site Analysis results and share them
- Solar radiation data for locations to recommend the type of solar system needed based on the average needs over the year
- Calculates Return on Investment and CO2 emissions avoided

Existing Approaches for shade measurement are hardwares (such as SolarPathfinder kit and SunEye shade tool) which are quite expensive and complex for an ordinary user.

## ANGLE OF ELEVATION (AoE) OF THE LOWEST SKY PIXEL
After the segmentation of all frames, a fixed number of frames out of all segmented frames will be extracted such that one frame corresponds to each unique azimuthal direction. AoE of the lowest sky pixel will be calculated from the orientational parameters of the smartphone. Referring to the figure below, as the Z1 vector always points to a particular pixel (red dot pixel) of a frame at any orientation, thus AoE of the red dot pixel can be found. Now, AoE of the lowest sky pixel (i.e., Green Dot) corresponding to each unique Azimuthal directions will be calculated with reference to the red dot pixel.

Using Azimuth and corresponding Angle of Elevation for obstructions, obstructed Solar Insolation will be measured and subtracted from the Total Solar Insolation to calculate Effective Solar Insolation (Instantaneous, Daily, Monthly, Yearly). For the calculation of the Total Solar Insolation, great existing methods are available which use Geostationary Satellite’s (Kalpana-1 and INSAT-3D) data (which considers the role of atmospheric constituents such as air molecule, aerosol, water vapor, ozone, weather, and cloud) available at Meteorological and Oceanographic Satellite Data Archival Centre’s (MOSDAC) website. We can integrate our solution with the existing methods of calculating solar insolation, but in order to complete the proposed solution, Solar Constant is considered as 1.361 KW/m2 for the measurement of Solar Insolation and other related parameters such as GHI, DNI, DHI, and optimal Tilt Angle of the panel.

## TECHNOLOGY STACK USE CASES

### SOFTWARE
- Programming Languages: Python, Java, Kotlin
- Python Libraries: OpenCV, Pytorch, fastai
- Technologies: Android, Deep Learning, Image Processing

### HARDWARE
- A Smartphone with Gyroscope, Accelerometer, Magnetometer, GPS, and Camera

### Android App
- Compare Site
- Share Analysis Result
- Add Electrical Appliances
- Energy Yield
- GHI/DHI
- Photovoltaic Requirement
- Return on Investment
- Site Analysis
- Cost
- Payback Period
- Suggests Photovoltaic
- Site Analysis
- Receiver
- Online Marketplace
- [Include]
- [Include]
- [Include]

### User
- Optimal Panel Position/Tilt
- Solar Insolation
- User
- Site Analysis
- Energy Yield
- Compare Site
- GHI/DNI/DHI
- Add Electrical Appliances
- Requirement
- Suggests Cost
- Return on Investment
- Payback Period
- Share Analysis Result

### ANDROID APP
- Another Site Analysis
- Online Marketplace
- Receiver

### DEPENDENCIES/SHOW STOPPERS
A more accurate method for the calculation of Solar Insolation is needed, such as the one used in Solar Calculator App (Space Applications Centre, ISRO), which uses remote sensing observations from Geostationary Satellites and considers...

## TECHNOLOGY STACK USE CASES

### SOFTWARE
- Programming Languages: Python, Java, Kotlin
- Python Libraries: OpenCV, Pytorch, fastai
- Technologies: Android, Deep Learning, Image Processing

### HARDWARE
- A Smartphone with Gyroscope, Accelerometer, Magnetometer, GPS, and Camera

### User
- Optimal Panel Position/Tilt
- Solar Insolation
- User
- Site Analysis
- Energy Yield
- Compare Site
- GHI/DNI/DHI
- Add Electrical Appliances
- Requirement
- Suggests Cost
- Return on Investment
- Payback Period
- Share Analysis Result

### ANDROID APP
- Another Site Analysis
- Online Marketplace
- Receiver

### DEPENDENCIES/SHOW STOPPERS
A more accurate method for the calculation of Solar Insolation is needed, such as the one used in Solar Calculator App (Space Applications Centre, ISRO), which uses remote sensing observations from Geostationary Satellites and considers various other factors that affects Solar Insolation.