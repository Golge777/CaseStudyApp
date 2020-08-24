# CaseStudyApp

This application is written for the case study given by a fintech company. Essentially, the application is consisted of two parts.\
<br />The upper half itself is designed as a solitary application in which operator pays for prompted sales requests carried out by QR codes.\
The lower half is also designed as a solitary app that simulates a POS machine that prompts sales request by creating unique QR codes.

![resim](https://user-images.githubusercontent.com/47951005/91014261-c00a4900-e5f1-11ea-8402-4393c304f22a.png)

In both of the applications, I tried to design a basic and alluring UI as much as I could to keep user interaction simple and non-bothersome. For instance, I employed progress bars to notify users that an ongoing operation is being processed.

The whole scenario starts with a sales request prompted from the POS application by entering the sales amount and clicking the sell button.\
After the click, the POS application posts the sales request and receives the according QR data. Inside applicaton, we transform this QR data into
a real QR code and get the following sales screen:

![resim](https://user-images.githubusercontent.com/47951005/91015106-46735a80-e5f3-11ea-9b20-40cc9f75c3bc.png)

Assuming that the QR code is somehow transmitted to the tank, the operator inside the tank can now pay.\
As soon as he clicks the pay button, the panel application shows the according amount, date & time to the operator to notify him about the payment details that are going to be processed.
