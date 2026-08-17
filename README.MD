# Smart Home Monitoring System

Mobile smart-home monitoring and control system developed for SCS 3311.

## Team Members

- Sanuth Wijayarathna 23002263 
- Kaveen Amarasekara 23000066 
- Haritha Gamage 23002042 

## Links

- Repository - https://github.com/KaveenAmarasekara/smart-home-monitoring-system
- APK - https://drive.google.com/file/d/1zGb_Kj5_NsoSasbHnZCXOWBTzr4F-SjJ/view
- Demo Video - https://drive.google.com/file/d/18Ay9kuLMEq_uRtiUDCXNvwlvZ1RgEO9M/view
- Report - https://drive.google.com/file/d/18B07mx4rQdFhSK12XOk5VEgD7XKxc8EB/view?usp=sharing

---------------------------

This contains the design and implementation of a Smart Home Monitoring and Control 
System developed as the SCS3311 mini-project. The system consists of two components: an 
Android mobile application built with Jetpack Compose and a companion web-based Hardware 
Simulator Dashboard, both connected to Google Firebase as the shared cloud backend. 
The application enables users to monitor and control heterogeneous IoT devices distributed 
across two floor plans, each represented as an interactive 4×4 grid. Device types include 
standard electrical outlets, multi switch gang units, scheduled lighting, safety critical appliances 
(clothing iron with automatic cutoff), and security cameras with mock feed display. All state 
changes synchronize bidirectionally in real time through Cloud Firestore listeners. The system 
records usage metrics and delivers alert notifications, with user configurable alert filtering stored 
per account in Firestore.

## Technology Stack

- Kotlin
- Jetpack Compose
- Firebase Authentication
- Cloud Firestore
- Cloud Functions
- Firebase Cloud Messaging
- React
- GitHub

## Repository Structure

- android-app
- hardware-simulator
- cloud-functions
- documentation
- assets
- diagrams
