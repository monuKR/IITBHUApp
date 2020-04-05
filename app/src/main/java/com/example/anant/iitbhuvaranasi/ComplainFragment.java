package com.example.anant.iitbhuvaranasi;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Base64;
import android.util.Log;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.VolleyLog;
import com.android.volley.toolbox.ImageRequest;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static android.app.Activity.RESULT_OK;
import static androidx.core.content.ContextCompat.checkSelfPermission;
import static com.android.volley.VolleyLog.TAG;
import static com.example.anant.iitbhuvaranasi.Constants.ADD_USER_URL;
import static com.example.anant.iitbhuvaranasi.Constants.KEY_ACTION;
import static com.example.anant.iitbhuvaranasi.Constants.KEY_Complaint_Anonymous;
import static com.example.anant.iitbhuvaranasi.Constants.KEY_Complaint_Description;
import static com.example.anant.iitbhuvaranasi.Constants.KEY_Complaint_Emailid;
import static com.example.anant.iitbhuvaranasi.Constants.KEY_Complaint_HostelName;
import static com.example.anant.iitbhuvaranasi.Constants.KEY_Complaint_IMAGE;
import static com.example.anant.iitbhuvaranasi.Constants.KEY_Complaint_Name;
import static com.example.anant.iitbhuvaranasi.Constants.KEY_Complaint_Subject;
import static com.example.anant.iitbhuvaranasi.Constants.KEY_Complaint_Type;
import static com.example.anant.iitbhuvaranasi.Constants.KEY_LOST_IMAGE;
import static com.example.anant.iitbhuvaranasi.HomeActivity.emailOfStudent;
import static com.example.anant.iitbhuvaranasi.HomeActivity.name_student;

public class ComplainFragment extends Fragment {


    private Toolbar toolbar;
    private ImageButton sendButton;
    private LinearLayout uploadedImageContainer;
    private String hostel;
    private String ComplaineeName,ComplaineeEmailaddress;
    private String Complainttype;
    private TextView complaineeName,complaineeEmailaddress;
    private static final int PICK_IMAGE_REQUEST = 1;
    private static final int CAPTURE_IMAGE_REQUEST = 2;
    private static final int CAMERA_PERMISSION_REQUEST = 3;
    private EditText subjectEditBox;
    private EditText issueBox;
    private CheckBox anonymousCheckbox;
    private String keepAnonymous;
    private RequestQueue mrequestqueue;
    private RequestQueue imageRequestqueue;
    private TextView removeImage;
    private Intent cameraIntent;
    private Dialog attachImageOption;
    Bitmap rbitmap;
    private ArrayList<String> UserImage;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable final ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.complain_fragment, container, false);


        mrequestqueue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
        imageRequestqueue = Volley.newRequestQueue(Objects.requireNonNull(getContext()));
        issueBox = view.findViewById(R.id.issue_box);
        toolbar = (Toolbar) Objects.requireNonNull(getActivity()).findViewById(R.id.toolbar);
        uploadedImageContainer = view.findViewById(R.id.uploaded_image_container);
        anonymousCheckbox = view.findViewById(R.id.anonymous_checkbox);
        removeImage = view.findViewById(R.id.remove_image);
        Spinner complainSpinner = view.findViewById(R.id.complain_spinner);
        final Spinner hostelSpinner = view.findViewById(R.id.hostel_spinner);
        ImageButton anonymousHelp = (ImageButton) view.findViewById(R.id.anonymous_help);
        subjectEditBox = view.findViewById(R.id.subject_edittext);
        ImageButton addImage = view.findViewById(R.id.add_image);
        complaineeName = view.findViewById(R.id.complainee_name);
        complaineeEmailaddress = view.findViewById(R.id.complainee_emailaddress);


        UserImage = new ArrayList<>();

        // Todo retrive Name,Email
        ComplaineeName = name_student;
        complaineeName.setText(ComplaineeName);
        ComplaineeEmailaddress = emailOfStudent;
        complaineeEmailaddress.setText(ComplaineeEmailaddress);
        complaineeEmailaddress.setVisibility(View.VISIBLE);
        keepAnonymous = "No";


        //Creating send button
        int endMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 24, getResources().getDisplayMetrics());
        sendButton = new ImageButton(getContext());
        Toolbar.LayoutParams sendLayoutParam = new Toolbar.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        sendLayoutParam.gravity = Gravity.END;
        sendLayoutParam.setMarginEnd(endMargin);
        sendButton.setLayoutParams(sendLayoutParam);
        sendButton.setBackground(getContext().getResources().getDrawable(R.drawable.ic_send_black_24dp));

        // List for complainttype & hostel
        List<String> complaints = new ArrayList<>();
        complaints.add(0, "Complain Regarding");
        complaints.add("Academics (UG)");
        complaints.add("Academics (PG)");
        complaints.add("Hostel");
        complaints.add("Security");
        complaints.add("Internet/Wifi");
        complaints.add("Infrastructure");
        complaints.add("College festivals");
        complaints.add("General Complain");
        complaints.add("Sports Council");
        complaints.add("FMC Council");
        complaints.add("SSC Council");
        complaints.add("SNTC Council");

       List<String> hostels = new ArrayList<>();
        hostels.add(0, "Your Hostel");
        hostels.add("Aryabhatta - I (A & B Block)");
        hostels.add("Aryabhatta - II (C & D Block)");
        hostels.add("A.S.N. Bose");
        hostels.add("Dhanrajgiri");
        hostels.add("Dr. C.V. Raman");
        hostels.add("Dr. S. Ramanujan");
        hostels.add("Gandhi Smriti Chhatravas (EXTENSION)");
        hostels.add("Gandhi Smriti Chhatravas (OLD)");
        hostels.add("IIT (BHU) Girl's Hostel – I");
        hostels.add("IIT (BHU) Girl's Hostel – II");
        hostels.add("IIT Boy's (Saluja)");
        hostels.add("Limbdi");
        hostels.add("Morvi");
        hostels.add("Rajputana");
        hostels.add("S.C. Dey");
        hostels.add("Vishwakarma");
        hostels.add("Vishweshvaraiya");
        hostels.add("Vivekananda");

        // Setting up adapters to spinners
        ArrayAdapter<String> complaintsAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, complaints) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {

                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                switch (position) {
                    case 0:

                        tv.setTypeface(null, Typeface.BOLD);
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, 52);
                        tv.setTextColor(getResources().getColor(R.color.colorPrimary));
                        break;
                    default:
                        tv.setTypeface(null, Typeface.NORMAL);
                        tv.setTextColor(Color.BLACK);
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, 48);
                        break;
                }
                return view;
            }
        };
        complaintsAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        complainSpinner.setAdapter(complaintsAdapter);

        ArrayAdapter<String> hostelAdapter = new ArrayAdapter(getContext(), android.R.layout.simple_spinner_item, hostels) {
            @Override
            public boolean isEnabled(int position) {
                if (position == 0) {
                    // Disable the first item from Spinner
                    // First item will be use for hint
                    return false;
                } else {
                    return true;
                }
            }

            @Override
            public View getDropDownView(int position, View convertView,
                                        ViewGroup parent) {
                View view = super.getDropDownView(position, convertView, parent);
                TextView tv = (TextView) view;
                switch (position) {
                    case 0:
                        tv.setTypeface(null, Typeface.BOLD);
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, 52);
                        tv.setTextColor(getResources().getColor(R.color.colorPrimary));
                        break;
                    default:
                        tv.setTypeface(null, Typeface.NORMAL);
                        tv.setTextColor(Color.BLACK);
                        tv.setTextSize(TypedValue.COMPLEX_UNIT_PX, 48);
                        break;
                }
                return view;
            }
        };
        hostelAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        hostelSpinner.setAdapter(hostelAdapter);

        complainSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                Complainttype = parent.getItemAtPosition(position).toString();

            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });
        hostelSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                                                    @Override
                                                    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                                                        hostel = parent.getItemAtPosition(position).toString();
                                                    }

                                                    @Override
                                                    public void onNothingSelected(AdapterView<?> parent) {

                                                    }
                                                }


        );

//        issueBox.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                issueBox.setBackground(null);
//                return false;
//            }
//        });

        // Anonymous Checkbox
        anonymousCheckbox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (anonymousCheckbox.isChecked()) {
                    keepAnonymous = "Keep Anonymous";
                    ComplaineeName = "A IIT(BHU) Student";
                    complaineeName.setText(ComplaineeName);
                    ComplaineeEmailaddress = emailOfStudent;
                    complaineeEmailaddress.setText(ComplaineeEmailaddress);
                    complaineeEmailaddress.setVisibility(View.GONE);
                }
                else {
                    keepAnonymous = "No";
                    // Todo retrive Name,Email

                    ComplaineeName = name_student;
                    complaineeName.setText(ComplaineeName);
                    ComplaineeEmailaddress = emailOfStudent;
                    complaineeEmailaddress.setText(ComplaineeEmailaddress);
                    complaineeEmailaddress.setVisibility(View.VISIBLE);
                }
            }
        });

        anonymousHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final AlertDialog.Builder warning = new AlertDialog.Builder(Objects.requireNonNull(getContext()));
                warning.setMessage("Your identity will be kept anonymous to complain authority.\n\nBut in case of any dispute/misuse of this facility your identity can be reveled to authorities.");
                warning.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        warning.show().dismiss();
                    }
                });

                warning.show();
            }
        });

        anonymousHelp.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getContext(), "Help", Toast.LENGTH_SHORT).show();
                return false;
            }
        });

        // Adding & Removing Image
        addImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(uploadedImageContainer.getChildCount()<5) {

                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                            LinearLayout.LayoutParams.WRAP_CONTENT);
                    attachImageOption = new Dialog(getContext());
                    LinearLayout linearLayout = (LinearLayout) LayoutInflater.from(getActivity().getApplicationContext()).inflate(R.layout.uploadimage_dialog_layout, null, false);


                    linearLayout.findViewById(R.id.attachimage).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            attachImage();
                        }
                    });


                    linearLayout.findViewById(R.id.captureimage).setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            captureImage();
                        }
                    });

                    attachImageOption.addContentView(linearLayout, params);

                    attachImageOption.show();
                }else {
                    Toast.makeText(getContext(),"You have reached your maximum upload limit", Toast.LENGTH_SHORT).show();
                }






            }
        });

        removeImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                for (int i = 1; i < uploadedImageContainer.getChildCount(); ) {
                    uploadedImageContainer.removeViewAt(i);
                }
                UserImage.clear();
                removeImage.setVisibility(View.GONE);
            }
        });

        // Send Button
        sendButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                Toast.makeText(getContext(), "Send Complain", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        sendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (Complainttype.equals("Complain Regarding")) {
                    Toast.makeText(getContext(), "Please specify complain type", Toast.LENGTH_SHORT).show();
                } else if (hostel.equals("Your Hostel")) {
                    Toast.makeText(getContext(), "Please select your hostel", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(subjectEditBox.getText())) {
//                    issueBox.setBackground(getResources().getDrawable(R.drawable.button_style));
                    issueBox.setPressed(true);
                    Toast.makeText(getContext(), "Please fill subject of complain", Toast.LENGTH_SHORT).show();
                } else if (TextUtils.isEmpty(issueBox.getText())) {
//                    issueBox.setBackground(getResources().getDrawable(R.drawable.button_style));
                    issueBox.setPressed(true);
                    Toast.makeText(getContext(), "Please describe your issue", Toast.LENGTH_SHORT).show();
                } else {
                    AlertDialog.Builder a_builder = new AlertDialog.Builder(getContext());
                    a_builder.setMessage("I am aware that if I will misuse this facility by any way I would be deregistered from this app");
                    a_builder.setCancelable(false);
                    a_builder.setPositiveButton("ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            final String message = issueBox.getText().toString();
                            final String subject = subjectEditBox.getText().toString();
                            final ProgressDialog pdialog = new ProgressDialog(getContext());
                            pdialog.setProgressStyle(ProgressDialog.STYLE_SPINNER);
                            pdialog.setMessage("Registering your complain....");
                            pdialog.show();
                            final String Complainttype1 =Complainttype.trim();
                            final String hostel1 =hostel.trim();

                            StringRequest stringRequest = new StringRequest(Request.Method.POST,ADD_USER_URL,
                                    new Response.Listener<String>() {
                                        @Override
                                        public void onResponse(String response) {
                                            pdialog.dismiss();
                                            //Toast.makeText(getContext(),response,Toast.LENGTH_LONG).show();
                                            if((response.toString()).equals("Success")) {
                                                Snackbar.make(Objects.requireNonNull(getView()), "Complain successfully Registered", Snackbar.LENGTH_LONG).show();
                                            }
                                            else if((response.toString()).equals("Block")) {
                                                Snackbar.make(Objects.requireNonNull(getView()), "Complain Registered but You are blocked for misuse of app Contact concerned authority", Snackbar.LENGTH_LONG).show();
                                            }

                                        }
                                    },
                                    new Response.ErrorListener() {
                                        @Override
                                        public void onErrorResponse(VolleyError error) {
                                            //Toast.makeText(getContext(),error.toString(),Toast.LENGTH_LONG).show();
                                            pdialog.dismiss();

                                            Snackbar.make(Objects.requireNonNull(getView()),"Something went Wrong\nTry again Later",Snackbar.LENGTH_LONG).show();

                                        }
                                    }){
                                @Override
                                protected Map<String,String> getParams(){
                                    Map<String,String> params = new HashMap<String, String>();
                                    params.put(KEY_ACTION,"insert");
                                    params.put(KEY_Complaint_Name,ComplaineeName);
                                    params.put(KEY_Complaint_Emailid,ComplaineeEmailaddress);
                                    params.put(KEY_Complaint_Type,Complainttype1);
                                    params.put(KEY_Complaint_HostelName,hostel1);
                                    params.put(KEY_Complaint_Subject,subject);
                                    params.put(KEY_Complaint_Description,message);
                                    params.put(KEY_Complaint_Anonymous,keepAnonymous);
                                    if(UserImage!=null) {
                                        for(int i=0;i<UserImage.size();i++) {
                                            params.put(KEY_LOST_IMAGE + i, UserImage.get(i));
                                        }
                                    }
                                    return params;
                                }

                            };

                            int socketTimeout = 30000; // 30 seconds. You can change it
                            RetryPolicy policy = new DefaultRetryPolicy(socketTimeout,
                                    DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                                    DefaultRetryPolicy.DEFAULT_BACKOFF_MULT);

                            stringRequest.setRetryPolicy(policy);


                            RequestQueue requestQueue = Volley.newRequestQueue(getContext());

                            requestQueue.add(stringRequest);


//
                        }
                    });

                    a_builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.cancel();


                        }
                    });
                    AlertDialog alert = a_builder.create();
                    alert.setTitle("Alert!");
                    alert.show();

                }
            }


        });

        return view;
    }

    // Working with Attach & remove Image
    private void attachImage() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
        intent.setAction(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST);
    }

    private void captureImage(){
        if (checkSelfPermission(Objects.requireNonNull(getContext()), Manifest.permission.CAMERA) == PackageManager.PERMISSION_DENIED) {
            ActivityCompat.requestPermissions(Objects.requireNonNull(getActivity()), new String[]{Manifest.permission.CAMERA}, CAMERA_PERMISSION_REQUEST);

        }else {
            cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(cameraIntent, CAPTURE_IMAGE_REQUEST);
        }

    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if(requestCode == CAMERA_PERMISSION_REQUEST ){
//            if(grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                cameraIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
//                startActivityForResult(cameraIntent, CAPTURE_IMAGE_REQUEST);
//            }else{
//                Toast.makeText(getContext(),"Permission Denied",Toast.LENGTH_SHORT).show();
//            }
//        }else {
//            Toast.makeText(getContext(),"Hi",Toast.LENGTH_SHORT).show();
//        }
//    }



    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        int width = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100, getResources().getDisplayMetrics());
        int margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());



        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK ) {
            assert data != null;
            ClipData mClipData = data.getClipData();


            if (mClipData != null && mClipData.getItemCount() > 1 && mClipData.getItemCount()< 6-uploadedImageContainer.getChildCount()) {

                for (int i = 0; i < mClipData.getItemCount(); i++) {
                    ImageView image = new ImageView(getContext());
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,
                            LinearLayout.LayoutParams.MATCH_PARENT);
                    params.setMargins(margin, margin, margin, margin);
                    image.setLayoutParams(params);
                    image.setScaleType(ImageView.ScaleType.CENTER_CROP);

                    Uri imageUri = mClipData.getItemAt(i).getUri();

                    try {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(),imageUri);
                        rbitmap = getResizedBitmap(bitmap,500);//Setting the Bitmap to ImageView
                        String userImage = getStringImage(rbitmap);
                        UserImage.add(userImage);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    image.setImageURI(imageUri);
                    uploadedImageContainer.addView(image);
                    removeImage.setVisibility(View.VISIBLE);
                }
            } else if(mClipData != null && mClipData.getItemCount() == 1){
                Uri imageUri = data.getData();

                try {
                    Bitmap bitmap = MediaStore.Images.Media.getBitmap(Objects.requireNonNull(getActivity()).getContentResolver(),imageUri);
                    rbitmap = getResizedBitmap(bitmap,500);//Setting the Bitmap to ImageView
                    String userImage = getStringImage(rbitmap);
                    //base64toString.add(userImage);
                    UserImage.add(userImage);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                ImageView image = new ImageView(getContext());
                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,
                        LinearLayout.LayoutParams.MATCH_PARENT);
                params.setMargins(margin, margin, margin, margin);
                image.setLayoutParams(params);
                image.setScaleType(ImageView.ScaleType.CENTER_CROP);

                image.setImageURI(imageUri);
                uploadedImageContainer.addView(image);
                removeImage.setVisibility(View.VISIBLE);
            }else{
                Toast.makeText(getContext(),"Your upload limit exceeded", Toast.LENGTH_SHORT).show();
            }

        }

        if (requestCode == CAPTURE_IMAGE_REQUEST && resultCode == RESULT_OK ) {
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            rbitmap = getResizedBitmap(bitmap,500);//Setting the Bitmap to ImageView
            String userImage = getStringImage(rbitmap);
            // base64toString.add(userImage);
            UserImage.add(userImage);

            ImageView image = new ImageView(getContext());
            LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(width,
                    LinearLayout.LayoutParams.MATCH_PARENT);
            params.setMargins(margin, margin, margin, margin);
            image.setLayoutParams(params);
            image.setScaleType(ImageView.ScaleType.CENTER_CROP);

            image.setImageBitmap(bitmap);

            uploadedImageContainer.addView(image);
            removeImage.setVisibility(View.VISIBLE);


        }

        attachImageOption.hide();

    }

//    private String imagetoString(Bitmap bitmap){
//        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
//        bitmap.compress(Bitmap.CompressFormat.JPEG,100,byteArrayOutputStream);
//        byte[] imgByte = byteArrayOutputStream.toByteArray();
//        return Base64.encodeToString(imgByte,Base64.DEFAULT);
//    }
private Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float)width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);

    }
    private String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
       bmp.compress(Bitmap.CompressFormat.JPEG, 100, baos);
        byte[] imageBytes = baos.toByteArray();

        return Base64.encodeToString(imageBytes, Base64.DEFAULT);
    }

    @Override
    public void onResume() {
        super.onResume();


        toolbar.setTitle("Complain");
        //        toolbar.setNavigationIcon(null);
//        ((HomeActivity) getActivity()).getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close_white_24dp);
//        ((HomeActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
//        ((HomeActivity) getActivity()).getSupportActionBar().setDisplayShowHomeEnabled(true);

        toolbar.addView(sendButton);


    }

    @Override
    public void onStop() {
        super.onStop();

        toolbar.removeView(sendButton);
        toolbar.setTitle(R.string.app_name);



    }



}
