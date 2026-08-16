# Project Roadmap (A-Z)

## Phase 1

├── Project Planning  
├── GitHub Repository  
├── Project Initialization  
├── Requirement Analysis  
├── UI Design  
├── Database Design  
└── Task Assignment  

## Phase 2

├── Android Development  
├── Firebase Backend  
├── Hardware Simulator  
└── Real-time Synchronization  

## Phase 3

├── Reporting  
├── Testing  
├── Documentation  
├── Video  
└── Final Submission  

# Technology Stack

## Mobile

Android Studio  
Kotlin  
Jetpack Compose  
MVVM Architecture  
Navigation Compose  
ViewModel  
StateFlow  
Coil (image loading)  

## Cloud

Firebase Authentication  
Firebase Firestore  
Firebase Cloud Messaging  
Cloud Functions  
Firebase Hosting (optional)  

## Hardware Simulator

React (recommended)  

or  

HTML + CSS + JavaScript  

Hosted using  

Firebase Hosting  

or  

Vercel  

# Version Control

Git  

GitHub  

GitHub Projects  

GitHub Issues  

GitHub Discussions  

# Suggested Repository Structure

```text
smart-home-system/

│
├── android-app/
│
├── hardware-simulator/
│
├── cloud-functions/
│
├── documentation/
│
├── assets/
│
├── diagrams/
│
├── README.md
│
└── .gitignore
```

# Week-by-Week Plan

## Week 1

Planning  

Repository  

Firebase  

UI  

Database Design  

## Week 2

Dashboard  

Floor Plans  

Realtime Sync  

Simulator  

## Week 3

Safety Rules  

Reports  

Notifications  

Testing  

## Week 4

Bug Fixes  

Documentation  

Video  

APK  

Submission  

# Work Division (3 Members)

## Member 1

Android Lead  

### Responsibilities

Project setup  
Navigation  
Floor dashboard  
Device UI  
Camera page  
Reports UI  
Final APK  

### Estimated

40%  

## Member 2

Backend Lead  

### Responsibilities

Firebase  
Firestore  
Authentication  
Cloud Functions  
Notifications  
Auto shutdown logic  

### Estimated

30%  

## Member 3

Simulator Lead  

### Responsibilities

Web simulator  
Database listener  
Device animation  
Documentation  
Testing  
Demo preparation  

### Estimated

30%  

Everyone should review pull requests so all members understand the complete system before the individual defense.  

# Git Workflow

Never push directly to main.  

```text
main

↓

develop

↓

feature/dashboard
feature/firebase
feature/simulator
feature/report
```

## Workflow

```text
feature

↓

Pull Request

↓

develop

↓

testing

↓

main
```

# Milestone 1

## Create GitHub Repository

### Repository

smart-home-monitoring-system  

### README should contain

Project  

Members  

Tech Stack  

Folder Structure  

Installation  

Screenshots  

License  

## Branches

main  

develop  

feature/android  

feature/backend  

feature/simulator  

## Git Ignore

Android Studio  

Node  

Firebase  

# Android Initialization

Create project  

Empty Activity  

Jetpack Compose  

Kotlin  

Minimum SDK 26  

Gradle Kotlin DSL  

Package  

com.yourgroup.smarthome  

## Add Dependencies

Navigation  

Lifecycle  

Firebase  

Compose  

Material3  

Coil  

Charts  

## Suggested Architecture

Presentation  

Domain  

Data  

Repository  

Firebase  

UI  

ViewModel  

## Folder Structure

```text
app

│

├── ui

│

├── screens

│

├── navigation

│

├── components

│

├── model

│

├── repository

│

├── firebase

│

├── viewmodel

│

├── util

│

└── MainActivity
```

# Firebase Initialization

Create project  

Smart Home Monitoring  

## Enable

Authentication  

Firestore  

Cloud Messaging  

Cloud Functions  

Hosting  

## Collections

users  

homes  

floors  

devices  

notifications  

reports  

# Initial Firestore Design

## homes

home1  

name  

owner  

members  

## floors

floor1  

name  

image  

rows  

columns  

## devices

device1  

name  

type  

status  

position  

maxOnDuration  

currentState  

lastUpdated  

# Hardware Simulator Initialization

Create React project  

hardware-simulator  

Install Firebase  

Connect Firestore  

Create Dashboard  

```text
Whenever Firestore changes

↓

Update icons

Whenever simulator changes

↓

Write Firestore

↓

Android updates automatically
```

# Documentation Folder

Documentation  

Requirement Analysis  

Database Design  

Architecture  

API  

Testing  

Meeting Notes  

Weekly Progress  

# UI Screens

```text
Splash

↓

Login

↓

Home

↓

Floor Selection

↓

Floor Dashboard

↓

Device Details

↓

Reports

↓

Notifications

↓

Settings
```

# Devices

## Outlet

ON  

OFF  

ERROR  

DISCONNECTED  

## Light

Brightness  

Schedule  

## Iron

Countdown  

Auto Shutdown  

## Camera

Snapshot  

Mock Stream  

## Switch Unit

Switch1  

Switch2  

Switch3  

Switch4  

Switch5  

# Cloud Function

```text
Every minute

↓

Read devices

↓

Find

status == ON

If

currentTime - turnedOnTime

>

maxOnDuration

Then

status = OFF

Send notification
```

# Reporting

Daily Usage  

Weekly Usage  

Monthly Usage  

Charts  

Most Used Device  

Power Consumption  

Auto Shutdown History  

# Testing Checklist

Login  

Device ON/OFF  

Floor loading  

Real-time synchronization  

Hardware simulator synchronization  

Auto shutdown  

Notification delivery  

Reports generation  

Camera mock stream  

Offline handling  

# GitHub Project Board

Create these columns:  

Backlog  

To Do  

In Progress  

Code Review  

Testing  

Done  

## Example issues

Setup Android project  

Configure Firebase  

Design Firestore schema  

Create Floor Dashboard  

Implement Device Card  

Add Multi-Switch Unit  

Implement Camera View  

Build Hardware Simulator  

Add Cloud Function for Safety Cutoff  

Generate Usage Reports  

Write Technical Documentation  

Record Demo Video  

# Documentation Deliverables

Your report should include:  

Introduction  

Functional Requirements  

System Architecture Diagram  

Firestore Database Design  

Synchronization Mechanism  

Floor Representation Method  

Hardware Simulator Design  

Safety Rule Implementation  

Testing Results  

Challenges and Future Improvements
