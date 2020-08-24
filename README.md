# CaseStudyApp

This application is written for the case study given by a fintech company. Essentially, the application is consisted of two parts.\
<br />The upper half itself is designed as a solitary application in which operator pays for prompted sales requests carried out by QR codes.\
The lower half is also designed as a solitary app that simulates a POS machine that prompts sales request by creating unique QR codes.

![resim](https://user-images.githubusercontent.com/47951005/91014261-c00a4900-e5f1-11ea-8402-4393c304f22a.png)

In both of the applications, I tried to design a basic and alluring UI as much as I could to keep user interaction simple and non-bothersome. For instance, I employed progress bars to notify users that an ongoing operation is being processed.

The whole scenario starts with a sales request prompted from the POS application by entering the sales amount and clicking the sell button.\
After the click, the POS application posts the sales request and receives the according QR data. Inside the applicaton, we transform this QR data into
a real QR code and get the following sales screen:

![resim](https://user-images.githubusercontent.com/47951005/91015106-46735a80-e5f3-11ea-9b20-40cc9f75c3bc.png)

Assuming that the QR code is somehow transmitted to the tank (i.e. by employing method X), the operator inside the tank can now pay.\
As soon as he clicks the pay button, the panel application shows the according amount, date & time to the operator to notify him about the payment details that are going to be processed.

![resim](https://user-images.githubusercontent.com/47951005/91015593-0e204c00-e5f4-11ea-860b-e15e8f91fe18.png)

After checking the amount, date & time, the operator can either cancel or approve the payment by clicking the buttons.\
If he approves the payment, the Tank Panel application sends a payment POST request and receives the according data from the server
to reveal if the payment is approved or not. If the payment is approved notifications appear on both applications to display that the transaction has been a successful one.

![resim](https://user-images.githubusercontent.com/47951005/91016391-5e4bde00-e5f5-11ea-906b-ec484ca953bc.png)

If the payment is not successful, notifications appear on both applications accordingly as follows:

![resim](https://user-images.githubusercontent.com/47951005/91016570-a1a64c80-e5f5-11ea-884d-8d4b0f31bb12.png)

Now everything is reset and the POS can make a new sales request.

## Method X

Even though a modern tank is equipped with plenty of cameras that see the entire surrounding of itself, these cameras cannot be used for reading QR codes. Adding a specific camera for QR code reading is also not plausible as it is really problematic to show the QR codes appearing on POS machines to the tank.\
Therefore, I believe that a new transmission method similar to automatic vehicle identification systems (i.e. taşıt tanıma sistemi) found on cars should be introduced. 
Near-Field communication chips can be a good idea to transmit the data and help carry out required protocols.
