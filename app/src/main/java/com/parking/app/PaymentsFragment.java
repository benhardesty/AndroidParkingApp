/*
 * Copyright © 2017 Benjamin Hardesty. All Rights Reserved.
*/

package com.parking.app;


import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SimpleAdapter;
import android.widget.Toast;

import com.braintreepayments.api.dropin.DropInActivity;
import com.braintreepayments.api.dropin.DropInRequest;
import com.braintreepayments.api.dropin.DropInResult;


import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 */
public class PaymentsFragment extends Fragment {

    MainActivity mainActivity;
    String ipAddress;
    String sessionid;
    private static View view;
    Button addCard;

    ListView cardsListView;
    SimpleAdapter simpleAdapter;
    List<Card> cards = new ArrayList<Card>();
    List<Map<String, String>> cardsList = new ArrayList<Map<String, String>>();
    JSONArray cardsArray;

    String clientToken;

    int BRAINTREE_REQUEST_CODE = 1;

    public PaymentsFragment() {
        // Required empty public constructor
    }

    @Override
    public void onResume() {
        refreshCards();
        super.onResume();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getActivity().setTitle("Payments");
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        //return inflater.inflate(R.layout.fragment_payments, container, false);

        // Inflate the layout for this fragment
        if(view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null) {
                parent.removeView(view);
            }
        }
        try {
            view = inflater.inflate(R.layout.fragment_payments, container, false);
        } catch (InflateException e) {

        }

        mainActivity = (MainActivity) getActivity();
        sessionid = mainActivity.sessionid;
        ipAddress = mainActivity.ipAddress;
        addCard = (Button) view.findViewById(R.id.add_card);
        cardsListView = (ListView) view.findViewById(R.id.current_cards_listview);

        cardsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                if (cards.size() == 0) {
                    return;
                }

                Bundle bundle = new Bundle();
                bundle.putParcelable("card", cards.get(i));
                bundle.putString("sessionid", sessionid);
                bundle.putString("previousActivity", "PaymentsFragment");
                bundle.putString("ipAddress", ipAddress);

                Intent intent = new Intent(getActivity(), ManagePaymentActivity.class);
                intent.putExtra("bundle", bundle);
                startActivity(intent);
            }
        });

        addCard.setVisibility(View.GONE);
        addCard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBraintreeSubmit(clientToken);
            }
        });

        try {
            String data = URLEncoder.encode("sessionid", "UTF-8") + "=" + URLEncoder.encode(sessionid, "UTF-8");
            AsyncUtility asyncUtility = new AsyncUtility(getActivity(), "", data, new AsyncUtility.AsyncResponse() {
                @Override
                public void processFinish(String output) {
                    clientToken = output;
                    addCard.setVisibility(View.VISIBLE);
                }
            });
            asyncUtility.execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        return view;
    }

    public void onBraintreeSubmit(String token) {
        DropInRequest dropInRequest = new DropInRequest()
                .clientToken(clientToken)
                .collectDeviceData(true)
                .disablePayPal();
        startActivityForResult(dropInRequest.getIntent(getActivity()), BRAINTREE_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == BRAINTREE_REQUEST_CODE) {
            if (resultCode == Activity.RESULT_OK) {
                // use the result to update your UI and send the payment method nonce to your server

                DropInResult result = data.getParcelableExtra(DropInResult.EXTRA_DROP_IN_RESULT);
                String paymentMethodNonce = result.getPaymentMethodNonce().getNonce();
                String deviceData = result.getDeviceData();
                addPaymentMethod(paymentMethodNonce, deviceData);
            } else if (resultCode == Activity.RESULT_CANCELED) {
                // the user canceled
            } else {
                // handle errors here
                Exception error = (Exception) data.getSerializableExtra(DropInActivity.EXTRA_ERROR);
            }
        }
    }

    /**
     * @param nonce
     * @param deviceData
     *
     * Add user credit or debit card. PayPal is disabled.
     */
    void addPaymentMethod(String nonce, String deviceData) {
        try {
            String data = URLEncoder.encode("sessionid", "UTF-8") + "=" + URLEncoder.encode(sessionid, "UTF-8") + "&" +
                    URLEncoder.encode("payment_method_nonce", "UTF-8") + "=" + URLEncoder.encode(nonce, "UTF-8") + "&" +
                    URLEncoder.encode("deviceData", "UTF-8") + "=" + URLEncoder.encode(deviceData, "UTF-8");
            AsyncUtility asyncUtility = new AsyncUtility(getActivity(), "", data, new AsyncUtility.AsyncResponse() {
                @Override
                public void processFinish(String output) {
                    JSONObject jsonObject = null;
                    try {
                        jsonObject = new JSONObject(output);
                        String message = jsonObject.getString("message");
                        Toast.makeText(getActivity(), message, Toast.LENGTH_SHORT).show();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            });
            asyncUtility.execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    /**
     * @param cardData Display user's masked cards
     */
    private void displayCardData(String cardData) {
        if (cardData.equals("")) {
            return;
        }

        JSONObject jsonObject = null;
        try {
            jsonObject = new JSONObject(cardData);
            String status = jsonObject.getString("status");

            if (status.equals("Success")) {
                cardsArray = jsonObject.getJSONArray("creditCards");

                for (int i = 0; i < cardsArray.length(); i++) {
                    Card card = new Card(cardsArray.getJSONObject(i));
                    cards.add(card);

                    Map<String, String> pair = new HashMap<String, String>(2);
                    pair.put("card", card.getCardString());
                    pair.put("blank", "");
                    cardsList.add(pair);
                }
                if (cardsList.size() == 0) {
                    Map<String, String> pair = new HashMap<String, String>(2);
                    pair.put("card", "No cards");
                    pair.put("blank", "");
                    cardsList.add(pair);
                }
                simpleAdapter = new SimpleAdapter(getActivity(), cardsList, android.R.layout.simple_list_item_2, new String[]{"card", "blank"}, new int[]{android.R.id.text1, android.R.id.text2});
                cardsListView.setAdapter(simpleAdapter);
            }
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    /**
     * Refresh payment cards on resume, after a payment method has been added.
     */
    public void refreshCards() {
        cards.clear();
        cardsList.clear();
        simpleAdapter = new SimpleAdapter(getActivity(), cardsList, android.R.layout.simple_list_item_2, new String[]{"Address", "Dates"}, new int[]{android.R.id.text1, android.R.id.text2});
        cardsListView.setAdapter(simpleAdapter);
        try {
            String data = URLEncoder.encode("sessionid", "UTF-8") + "=" + URLEncoder.encode(sessionid, "UTF-8");
            AsyncUtility asyncUtility = new AsyncUtility(getActivity(), "", data, new AsyncUtility.AsyncResponse() {
                @Override
                public void processFinish(String output) {
                    displayCardData(output);
                }
            });
            asyncUtility.execute();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

}
