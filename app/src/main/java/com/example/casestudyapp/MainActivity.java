package com.example.casestudyapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.os.Handler;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.security.SecureRandom;
import java.security.cert.X509Certificate;
import java.util.Map;
import java.util.HashMap;

import javax.net.ssl.HostnameVerifier;
import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSession;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

import androidmads.library.qrgenearator.QRGContents;
import androidmads.library.qrgenearator.QRGEncoder;

public class MainActivity extends AppCompatActivity {

    private Context context;

    // I tried to design a basic and alluring UI as much as I could to keep user interaction simple and non-bothersome.
    // For instance, I employed progress bars to notify users that an ongoing operation is being processed.
    // Declarations of UI elements located in both Tank Panel & POS applications
    private Button buttonCheck;
    private ImageView imageViewOpet;
    private ProgressBar progressBar;
    private ProgressBar progressBarTsk;
    private EditText editTextAmount;
    private TextView textViewPosMessage;
    private Button buttonSell;
    private Dialog dialogEdit;
    private TextView textViewDialogEdit;
    private Button buttonEditCancel;
    private Button buttonEditOK;
    ConstraintLayout constraintLayoutDialogEdit;
    TextView textViewDialogEditHeader;
    Window windowEdit;
    WindowManager.LayoutParams wlpEdit;

    // Dummy variable declarations to parse API responses correctly
    private String qrData;
    private String originalQRData;
    private String paymentDate;
    private String paymentTime;
    private String theAmount;
    private String paymentAmount;
    private int theAmountReceived;
    private RequestQueue requestQueue;
    private String urlGET;
    private String urlPayment;
    private boolean isAnyPaymentAwaited;
    private boolean isPaymentSuccessful;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Initializations
        context = getApplicationContext();
        isAnyPaymentAwaited = false;
        isPaymentSuccessful = false;

        requestQueue = Volley.newRequestQueue(this);

        urlGET = "https://sandbox-api.payosy.com/api/get_qr_sale";
        urlPayment = "https://sandbox-api.payosy.com/api/payment";


        // Below code block is written to bypass SSL verifications
        try {
            TrustManager[] victimizedManager = new TrustManager[]{

                    new X509TrustManager() {

                        public X509Certificate[] getAcceptedIssuers() {

                            X509Certificate[] myTrustedAnchors = new X509Certificate[0];

                            return myTrustedAnchors;
                        }

                        @Override
                        public void checkClientTrusted(X509Certificate[] certs, String authType) {
                        }

                        @Override
                        public void checkServerTrusted(X509Certificate[] certs, String authType) {
                        }
                    }
            };

            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, victimizedManager, new SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
            HttpsURLConnection.setDefaultHostnameVerifier(new HostnameVerifier() {
                @Override
                public boolean verify(String s, SSLSession sslSession) {
                    return true;
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }

        //UI element initializations
        progressBarTsk = findViewById(R.id.loading_spinner_tsk);
        progressBarTsk.setVisibility(View.INVISIBLE);
        buttonCheck = findViewById(R.id.buttonCheck);
        imageViewOpet = findViewById(R.id.imageViewOpet);
        progressBar = findViewById(R.id.loading_spinner);
        progressBar.setVisibility(View.INVISIBLE);
        textViewPosMessage = findViewById(R.id.posMessage);
        textViewPosMessage.setVisibility(View.INVISIBLE);
        editTextAmount = findViewById(R.id.editTextAmount);
        dialogEdit = new Dialog(this);
        dialogEdit.setContentView(R.layout.dialog_approval);
        LayoutInflater factory = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE); // We use LAYOUT_INFLATER_SERVICE to inflate a pop-up dialog window on top of the main activity
        constraintLayoutDialogEdit = (ConstraintLayout) dialogEdit.findViewById(R.id.constraintLayoutDialogEdit);
        textViewDialogEdit = (TextView) constraintLayoutDialogEdit.findViewById(R.id.textViewDialogEdit);
        textViewDialogEditHeader = (TextView) constraintLayoutDialogEdit.findViewById(R.id.textViewDialogEditHeader);
        buttonEditCancel = (Button) constraintLayoutDialogEdit.findViewById(R.id.buttonEditCancel);
        buttonEditOK = (Button) constraintLayoutDialogEdit.findViewById(R.id.buttonEditOK);
        buttonSell = findViewById(R.id.buttonSell);
        //To be able to configure pop-up dialog window to our liking
        windowEdit = dialogEdit.getWindow();
        wlpEdit = windowEdit.getAttributes();
        wlpEdit.gravity = Gravity.TOP;
        wlpEdit.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        windowEdit.setAttributes(wlpEdit);

        //By adding the below Text Change Listener, we prevent end-users to enter nothing but payment values
        //that are ranged between 1-999 Turkish Liras
        //Additionally, we prevent enter of undesired inputs such as 001, 012 (i.e. the inputs that start with 0)
        editTextAmount.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {
                String textEntered = editTextAmount.getText().toString().trim();
                if(!textEntered.equals("")){
                    int num = Integer.parseInt(textEntered);
                    if(num>999)
                    {
                        editTextAmount.setText("999");
                    }
                    if(num<1)
                    {
                        editTextAmount.setText("1");
                    }
                    if (editable.toString().length() > 1 && editable.toString().startsWith("0")) {
                        editable.clear();
                    }
                }

                theAmount = editTextAmount.getText().toString().trim();
            }
        });

        //Declaration of button OnClick Listeners

        buttonSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //If nothing has been entered as payment value, notify the user by sending a Toast notification
                if(editTextAmount.getText().toString().equals(""))
                {
                    Toast.makeText(context, "Lütfen satış tutarını giriniz.", Toast.LENGTH_SHORT).show();
                    return;
                }

                //If a value is entered, the POS application should move to displaying QR code and "payment-awaiting" screen.
                //Here I don't make the POS application to pass to new activity. I basically create new screens by setting visibilities of appropriate UI elements.
                theAmount = editTextAmount.getText().toString().trim();
                buttonSell.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                //Here we make our POST request
                PostPOSRequest(theAmount);
                isAnyPaymentAwaited = true;
            }
        });

        buttonEditCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                //First of all, we need to clear the EditText to prompt the user to enter a new value
                editTextAmount.setText("");
                progressBarTsk.setVisibility(View.VISIBLE);
                CancelApprovalDialog();

                //After clicking the cancel button, I deliberately put a realistic 1.5 seconds delay to simulate server responses to both Tank Panel & POS applications
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                            isAnyPaymentAwaited = false;
                            progressBarTsk.setVisibility(View.INVISIBLE);
                            buttonSell.setVisibility(View.VISIBLE);
                            buttonCheck.setVisibility(View.VISIBLE);
                            textViewPosMessage.setVisibility(View.INVISIBLE);
                            imageViewOpet.setImageResource(R.drawable.opetlogo);
                            Toast toast = Toast.makeText(context, "Ödeme İptal Edildi!", Toast.LENGTH_LONG);
                            toast.setGravity((Gravity.TOP|Gravity.CENTER_HORIZONTAL), 0, 350);
                            toast.show();
                            Toast.makeText(context, "Ödeme Başarısız!", Toast.LENGTH_LONG).show();
                    }
                }, 1500);

            }
        });

        buttonEditOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                PostPaymentRequest(theAmountReceived);
                CancelApprovalDialog();
                buttonCheck.setVisibility(View.VISIBLE);
                progressBar.setVisibility(View.VISIBLE);
                progressBarTsk.setVisibility(View.VISIBLE);

                //Again, I deliberately put a realistic 3 seconds delay to simulate server responses to both Tank Panel & POS applications
                new Handler().postDelayed(new Runnable() {
                    @Override
                    public void run() {

                        //We check whether the payment successful or not inside our PostPaymentRequest. It basically reads the server response to see if the payment is OK and returned with the correct code.
                        //Regardless of the payment result, we reset the UI to prepare for a new payment.
                        //For this purpose, we clear EditText and show the sell button to be able to accept new payment requests.
                        if(isPaymentSuccessful)
                        {
                            isAnyPaymentAwaited = false;
                            progressBar.setVisibility(View.INVISIBLE);
                            progressBarTsk.setVisibility(View.INVISIBLE);

                            editTextAmount.setText("");
                            buttonSell.setVisibility(View.VISIBLE);
                            textViewPosMessage.setVisibility(View.INVISIBLE);
                            imageViewOpet.setImageResource(R.drawable.opetlogo);

                            Toast toast = Toast.makeText(context, "Ödeme Başarılı!", Toast.LENGTH_LONG);
                            toast.setGravity((Gravity.TOP|Gravity.CENTER_HORIZONTAL), 0, 350);
                            toast.show();

                            Toast.makeText(context, "Ödeme Başarılı!", Toast.LENGTH_LONG).show();

                        }
                        else
                        {

                            isAnyPaymentAwaited = false;
                            progressBar.setVisibility(View.INVISIBLE);
                            progressBarTsk.setVisibility(View.INVISIBLE);

                            editTextAmount.setText("");
                            buttonSell.setVisibility(View.VISIBLE);
                            textViewPosMessage.setVisibility(View.INVISIBLE);
                            imageViewOpet.setImageResource(R.drawable.opetlogo);

                            Toast toast = Toast.makeText(context, "Ödeme Başarısız!", Toast.LENGTH_LONG);
                            toast.setGravity((Gravity.TOP|Gravity.CENTER_HORIZONTAL), 0, 350);
                            toast.show();

                            Toast.makeText(context, "Ödeme Başarısız!", Toast.LENGTH_LONG).show();
                        }
                    }
                }, 3000);

            }
        });

        //First of all, by checking the "simulated" server response isAnyPaymentAwaited we check if any sales has been prompted
        buttonCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(!isAnyPaymentAwaited)
                {
                    Toast toast = Toast.makeText(context, "QR kod bulunamadı...", Toast.LENGTH_LONG);
                    toast.setGravity((Gravity.TOP|Gravity.CENTER_HORIZONTAL), 0, 350);
                    toast.show();

                    return;
                }

                //If there is a sales prompt, then we create a pop-up window for the approval of the prompted sales
                ShowApprovalDialog();
                buttonCheck.setVisibility(View.INVISIBLE);

            }
        });
    }

    //This where we send our request for a QR code of desired sales
    //We carry out this request from our POS application
    private void PostPOSRequest(final String theAmount)
    {
        JSONObject jsonObject = new JSONObject();
        try
        {
            jsonObject.put("totalReceiptAmount", Integer.valueOf(theAmount));

        } catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

    JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, urlGET, jsonObject, new Response.Listener<JSONObject>() {
        @Override
        public void onResponse(JSONObject response) {

            Log.d("accessToken:",response.toString());
            qrData = response.toString().substring(46);
            originalQRData = qrData.substring(1, qrData.length() - 2);

            //The three lines written below are added to show actual QR codes inside the POS application
            QRGEncoder qrgEncoder = new QRGEncoder(qrData, null, QRGContents.Type.TEXT, 10);
            Bitmap qrBits = qrgEncoder.getBitmap();
            imageViewOpet.setImageBitmap(qrBits);

            progressBar.setVisibility(View.INVISIBLE);
            textViewPosMessage.setVisibility(View.VISIBLE);

            // Since the amount varies between 1-1000, server response changes accordingly.
            // To read the information received correctly, we make the following adjustments.
            if(Integer.valueOf(theAmount)<10)
            {
                paymentAmount = "Ödenecek Tutar: " + qrData.substring(18, 19) + " TL";
                theAmountReceived = Integer.valueOf(qrData.substring(18, 19));
                paymentDate = qrData.substring(35,45);
                paymentTime = qrData.substring(46,54);
            }
            else if(9 < Integer.valueOf(theAmount) && Integer.valueOf(theAmount) < 100)
            {
                paymentAmount = "Ödenecek Tutar: " + qrData.substring(18, 20) + " TL";
                theAmountReceived = Integer.valueOf(qrData.substring(18, 20));
                paymentDate = qrData.substring(36,46);
                paymentTime = qrData.substring(47,55);
            }
            else if(Integer.valueOf(theAmount) > 100)
            {
                paymentAmount = "Ödenecek Tutar: " + qrData.substring(18, 21) + " TL";
                theAmountReceived = Integer.valueOf(qrData.substring(18, 21));
                paymentDate = qrData.substring(37,47);
                paymentTime = qrData.substring(48,56);
            }

            //To show the tank operator the payment amount, date & time to prevent him approve an undesired payment
            textViewDialogEdit.setText("");
            textViewDialogEdit.setText(paymentAmount + "\n" + paymentDate + " | " + paymentTime);
        }
    },
            new Response.ErrorListener() {
                @Override
                public void onErrorResponse(VolleyError volleyError) {
                    Log.d("error:",volleyError.toString());

                }
            }){

        @Override
        public Map<String, String> getHeaders() throws AuthFailureError {
            Map<String,String> headers=new HashMap<String,String>();
            headers.put("accept","application/json");
            headers.put("content-type","application/json");
            headers.put("x-ibm-client-id","d56a0277-2ee3-4ae5-97c8-467abeda984d");
            headers.put("x-ibm-client-secret","U1yT3qD2jW6oO4uH8gB8bN1xW0xH3aL7jN2lT7dP5aL5rQ1vK4");
            return headers;
        }

    };
        requestQueue.add(postRequest);
    }

    //This where we send our payment request
    //We carry out this request from our Tank Panel application
    private void PostPaymentRequest(final int theAmountReceived)
    {

        JSONObject root = new JSONObject();

        try
        {

        root.put("returnCode", 1000);
        root.put("returnDesc", "success");
        root.put("receiptMsgCustomer", "beko Campaign/n2018");
        root.put("receiptMsgMerchant", "beko Campaign Merchant/n2018");

        JSONObject paymentActionList = new JSONObject();

        paymentActionList.put("paymentType", 3);
        paymentActionList.put("amount", theAmountReceived);
        paymentActionList.put("currencyID", 949);
        paymentActionList.put("vatRate", 800);

        JSONArray paymentActionListArray = new JSONArray();

        paymentActionListArray.put(paymentActionList);

        JSONObject paymentInfoList = new JSONObject();

        paymentInfoList.put("paymentProcessorID", 67);
        paymentInfoList.put("paymentActionList", paymentActionListArray);

        JSONArray paymentInfoListArray = new JSONArray();

        paymentInfoListArray.put(paymentInfoList);

        root.put("paymentInfoList", paymentInfoListArray);
        root.put("QRdata",  originalQRData);
        Log.d("tag", root.toString());



        } catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        JsonObjectRequest postPaymentRequest = new JsonObjectRequest(Request.Method.POST, urlPayment, root, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                //This where we control whether the server response for our payment request is accepted or not
                //Again we simulate the server response to the POS application with a dummy variable called isPaymentSuccessful
                Log.d("accessToken2",response.toString());
                if(response.toString().contains("OK") && response.toString().contains("1000"))
                    isPaymentSuccessful = true;


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error:",error.toString());
                isPaymentSuccessful = false;
            }

        })
        {
            @Override
            public Map<String, String> getHeaders() throws AuthFailureError {
                Map<String,String> headers=new HashMap<String,String>();
                headers.put("accept","application/json");
                headers.put("content-type","application/json");
                headers.put("x-ibm-client-id","d56a0277-2ee3-4ae5-97c8-467abeda984d");
                headers.put("x-ibm-client-secret","U1yT3qD2jW6oO4uH8gB8bN1xW0xH3aL7jN2lT7dP5aL5rQ1vK4");
                return headers;
            }


        };

        requestQueue.add(postPaymentRequest);

    }

    private void ShowApprovalDialog()
    {
        dialogEdit.show();
    }
    private void CancelApprovalDialog()
    {
        dialogEdit.dismiss();
    }

}