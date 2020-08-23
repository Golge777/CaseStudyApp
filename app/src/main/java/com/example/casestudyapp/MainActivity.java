package com.example.casestudyapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;

import android.app.Dialog;
import android.graphics.Bitmap;
import android.os.Bundle;
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
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;

import android.util.Log;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
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

    private TextView textViewResults;
    private Button buttonCheck;
    private ImageView imageViewOpet;
    private ProgressBar progressBar;
    private EditText editTextAmount;
    private TextView textViewPosMessage;
    private Button buttonSell;
    private RequestQueue requestQueue;
    private RequestQueue requestQueue2;
    private String urlGET;
    private String urlPayment;

    private String qrData;
    private String originalQRData;
    private String paymentDate;
    private String paymentTime;
    private String theAmount;
    private String paymentAmount;
    private int theAmountReceived;

    private Dialog dialogEdit;

    private TextView textViewDialogEdit;
    private ImageView imageViewDialogEdit;
    private Button buttonEditCancel;
    private Button buttonEditOK;

    ConstraintLayout constraintLayoutDialogEdit;
    TextView textViewDialogEditHeader;

    LayoutInflater layoutInflater;

    Window windowEdit; // Dialog Edit menüsünün yukarıda çıkmasını sağlamak için
    WindowManager.LayoutParams wlpEdit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        requestQueue = Volley.newRequestQueue(this);
        requestQueue2 = Volley.newRequestQueue(this);

        theAmount = "100";

        urlGET = "https://sandbox-api.payosy.com/api/get_qr_sale";
        urlPayment = "https://sandbox-api.payosy.com/api/payment";

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


        textViewResults = findViewById(R.id.textViewResults);
        buttonCheck = findViewById(R.id.buttonCheck);

        imageViewOpet = findViewById(R.id.imageViewOpet);
        progressBar = findViewById(R.id.loading_spinner);
        progressBar.setVisibility(View.INVISIBLE);
        textViewPosMessage = findViewById(R.id.posMessage);
        textViewPosMessage.setVisibility(View.INVISIBLE);
        editTextAmount = findViewById(R.id.editTextAmount);
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

        dialogEdit = new Dialog(this); // Dialog Edit Initialization
        dialogEdit.setContentView(R.layout.dialog_approval);

        LayoutInflater factory = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);

        constraintLayoutDialogEdit = (ConstraintLayout) dialogEdit.findViewById(R.id.constraintLayoutDialogEdit);

        textViewDialogEdit = (TextView) constraintLayoutDialogEdit.findViewById(R.id.textViewDialogEdit);
        textViewDialogEditHeader = (TextView) constraintLayoutDialogEdit.findViewById(R.id.textViewDialogEditHeader);

        buttonEditCancel = (Button) constraintLayoutDialogEdit.findViewById(R.id.buttonEditCancel);
        buttonEditOK = (Button) constraintLayoutDialogEdit.findViewById(R.id.buttonEditOK);

        windowEdit = dialogEdit.getWindow(); // Initialization of the gravity of the custom popup window
        wlpEdit = windowEdit.getAttributes();

        wlpEdit.gravity = Gravity.TOP;
        wlpEdit.flags &= ~WindowManager.LayoutParams.FLAG_DIM_BEHIND;
        windowEdit.setAttributes(wlpEdit);

        buttonSell = findViewById(R.id.buttonSell);

        buttonSell.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                textViewResults.setText("");
                theAmount = editTextAmount.getText().toString().trim();
                buttonSell.setVisibility(View.INVISIBLE);
                progressBar.setVisibility(View.VISIBLE);

                PostPOSRequest(theAmount);
            }
        });

        buttonEditCancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                CancelApprovalDialog();
                buttonCheck.setVisibility(View.VISIBLE);
            }
        });

        buttonEditOK.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //textViewResults.setText("***"+String.valueOf(theAmountReceived)+"***");
                PostPaymentRequest(theAmountReceived);
                CancelApprovalDialog();
                buttonCheck.setVisibility(View.VISIBLE);
            }
        });

        buttonCheck.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                if(textViewPosMessage.getVisibility() != View.VISIBLE)
                {
                    Log.d("s", "asdasd");
                    return;
                }

                ShowApprovalDialog();
                buttonCheck.setVisibility(View.INVISIBLE);



            }
        });
    }

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
            //textViewResults.setText(response);
            qrData = response.toString().substring(46);
            originalQRData = qrData.substring(1, qrData.length() - 2);
            //textViewResults.setText(originalQRData);

            QRGEncoder qrgEncoder = new QRGEncoder(qrData, null, QRGContents.Type.TEXT, 10);

            Bitmap qrBits = qrgEncoder.getBitmap();
            imageViewOpet.setImageBitmap(qrBits);
            progressBar.setVisibility(View.INVISIBLE);
            textViewPosMessage.setVisibility(View.VISIBLE);
            //textViewResults.setText(qrData);
            //textViewResults.setText(theAmount);

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
                textViewResults.setText(theAmount+"***");
                paymentAmount = "Ödenecek Tutar: " + qrData.substring(18, 21) + " TL";
                theAmountReceived = Integer.valueOf(qrData.substring(18, 21));
                paymentDate = qrData.substring(37,47);
                paymentTime = qrData.substring(48,56);
            }

            //textViewResults.setText(String.valueOf(theAmountReceived));
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
        paymentActionList.put("amount", 100);
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
        textViewResults.setText(originalQRData);
        root.put("QRdata",  originalQRData);
        Log.d("tag", root.toString());



        } catch (JSONException e)
        {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        //Toast.makeText(this, "Hey", Toast.LENGTH_LONG).show(); -- Buraya kadar hatasız
        JsonObjectRequest postPaymentRequest = new JsonObjectRequest(Request.Method.POST, urlPayment, root, new Response.Listener<JSONObject>() {

            @Override
            public void onResponse(JSONObject response) {

                Log.d("accessToken2:",response.toString());

            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                Log.d("error:",error.toString());
                //String simulatedResponse =
                //Log.d("accessToken:", );
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