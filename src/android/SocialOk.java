package ru.trilan.socialok;

import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONArray;

import org.apache.cordova.CallbackContext;
import org.apache.cordova.CordovaArgs;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.PluginResult;

import android.content.Intent;
import android.content.Context;
import android.widget.Toast;
import android.util.Log;
import android.os.AsyncTask;
import android.app.Activity;
import java.util.HashMap;
import java.util.Map;
import java.util.EnumSet;
import java.io.IOException;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.content.pm.Signature;

import ru.ok.android.sdk.OkRequestMode;
import ru.ok.android.sdk.Odnoklassniki;
import ru.ok.android.sdk.Shared;
import ru.ok.android.sdk.OkAuthActivity;
import ru.ok.android.sdk.OkListener;
import ru.ok.android.sdk.util.OkScope;
import ru.ok.android.sdk.util.OkDevice;
import ru.ok.android.sdk.util.OkAuthType;

public class SocialOk extends CordovaPlugin {
    private static final String TAG = "SocialOk";
    private static final String ACTION_INIT = "initSocialOk";
    private static final String ACTION_LOGIN = "login";
    private static final String ACTION_LOGOUT = "logout";
    private static final String ACTION_SHARE = "share";
    private static final String ACTION_FRIENDS_GET = "friendsGet";
    private static final String ACTION_FRIENDS_GET_ONLINE = "friendsGetOnline";
    private static final String ACTION_STREAM_PUBLISH = "streamPublish";
    private static final String ACTION_USERS_GET_INFO = "usersGetInfo";
    private static final String ACTION_CALL_API_METHOD = "callApiMethod";
    private static final String ACTION_REPORT_PAYMENT = "reportPayment";
    private static final String ACTION_INSTALL_SOURCE = "getInstallSource";
    private static final String ACTION_PERFORM_POSTING = "performPosting";
    private static final String ACTION_PERFORM_SUGGEST = "performSuggest";
    private static final String ACTION_PERFORM_INVITE = "performInvite";
    private static final String IS_OK_APP_INSTALLED = "isOkAppInstalled";
    private static final String ACTION_REPORT_STATS = "reportStats";
    private Odnoklassniki odnoklassnikiObject;
    private CallbackContext _callbackContext;
    private String REDIRECT_URL = "";
    private JSONArray lastLoginPermissions = null;
    private String mAppId;
    private String mAppKey;

    private static final String ODKL_APP_SIGNATURE = "3082025b308201c4a00302010202044f6760f9300d06092a864886f70d01010505003071310c300a06035504061303727573310c300a06035504081303737062310c300a0603550407130373706231163014060355040a130d4f646e6f6b6c6173736e696b6931143012060355040b130b6d6f62696c65207465616d311730150603550403130e416e647265792041736c616d6f763020170d3132303331393136333831375a180f32303636313232313136333831375a3071310c300a06035504061303727573310c300a06035504081303737062310c300a0603550407130373706231163014060355040a130d4f646e6f6b6c6173736e696b6931143012060355040b130b6d6f62696c65207465616d311730150603550403130e416e647265792041736c616d6f7630819f300d06092a864886f70d010101050003818d003081890281810080bea15bf578b898805dfd26346b2fbb662889cd6aba3f8e53b5b27c43a984eeec9a5d21f6f11667d987b77653f4a9651e20b94ff10594f76a93a6a36e6a42f4d851847cf1da8d61825ce020b7020cd1bc2eb435b0d416908be9393516ca1976ff736733c1d48ff17cd57f21ad49e05fc99384273efc5546e4e53c5e9f391c430203010001300d06092a864886f70d0101050500038181007d884df69a9748eabbdcfe55f07360433b23606d3b9d4bca03109c3ffb80fccb7809dfcbfd5a466347f1daf036fbbf1521754c2d1d999f9cbc66b884561e8201459aa414677e411e66360c3840ca4727da77f6f042f2c011464e99f34ba7df8b4bceb4fa8231f1d346f4063f7ba0e887918775879e619786728a8078c76647ed";


    /**
     * Gets the application context from cordova's main activity.
     * @return the application context
     */
    /*
    private Context getApplicationContext() {
        return this.webView.getContext();
    }
    */

    private Context getApplicationContext() {
        return this.getActivity().getApplicationContext();
    }
    
    private Activity getActivity() {
        return (Activity)this.webView.getContext();
    }

    private void success(String status, CallbackContext context) {
        if(status == null) status = "Ok";
        if(context == null) context = _callbackContext;
        Log.i(TAG, "Operation completed with status: "+status);
        if(context != null) {
            try {
                JSONObject ob = new JSONObject(status);
                context.success(ob);
            } catch (Exception e1) {
                try {
                    JSONArray ar = new JSONArray(status);
                    context.success(ar);
                } catch (Exception e2) {
                    context.success(status);
                }
            }
        }
    }
    private void fail(String err, CallbackContext context) {
        if(err == null) err = "Error";
        if(context == null) context = _callbackContext;
        Log.e(TAG, "Operation failed with error: "+err);
        if(context != null) {
            try {
                JSONObject ob = new JSONObject(err);
                context.error(ob);
            } catch (Exception e) {
                context.error(err);
            }
        }
    }

    @Override
    protected void pluginInitialize() {
        //this.cordova.setActivityResultCallback(this);
    }

    @Override
    public boolean execute(String action, CordovaArgs args, final CallbackContext callbackContext) throws JSONException {
        Log.i(TAG, "Do action: "+action);
        if(ACTION_INIT.equals(action)) {
            return init(args.getString(0), args.getString(1), callbackContext);
        } else if (ACTION_LOGIN.equals(action)) {
            JSONArray permissions = args.optJSONArray(0);
            return login(permissions, callbackContext);
        } else if (ACTION_LOGOUT.equals(action)) {
            return logout(callbackContext);
        } else if (ACTION_SHARE.equals(action)) {
            return share(args.getString(0), args.getString(1), callbackContext);
        } else if (ACTION_FRIENDS_GET.equals(action)) {
            return friendsGet(args.getString(0), args.getString(1), callbackContext);
        } else if (ACTION_FRIENDS_GET_ONLINE.equals(action)) {
            return friendsGetOnline(args.getString(0), args.getString(1), callbackContext);
        } else if (ACTION_STREAM_PUBLISH.equals(action)) {
            // TODO
        } else if (ACTION_USERS_GET_INFO.equals(action)) {
            return usersGetInfo(args.getString(0), args.getString(1), callbackContext);
        } else if (ACTION_CALL_API_METHOD.equals(action)) {
            String method = args.getString(0);
            JSONObject params = args.getJSONObject(1);
            return callApiMethod(method, JsonHelper.toMap(params), callbackContext);
        } else if (ACTION_REPORT_PAYMENT.equals(action)) {
            String trx_id = args.getString(0);
            String amount = args.getString(1);
            String currency = args.getString(2);
            Map<String, String> params = new HashMap<String, String>();
            params.put("trx_id", trx_id);
            params.put("amount", amount);
            params.put("currency", currency);
            return callApiMethod("sdk.reportPayment", params, callbackContext);
        } else if(ACTION_INSTALL_SOURCE.equals(action)) {
            Map<String, String> params = new HashMap<String, String>();
            params.put("adv_id", OkDevice.getAdvertisingId(webView.getContext()));
            return getApiMethod("sdk.getInstallSource", params, callbackContext);
        } else if (IS_OK_APP_INSTALLED.equals(action)) {
            // check if OK application installed
            boolean ssoAvailable = false;
            final Intent intent = new Intent();
            intent.setClassName("ru.ok.android", "ru.ok.android.external.LoginExternal");
            final ResolveInfo resolveInfo = getApplicationContext().getPackageManager().resolveActivity(intent, 0);
            if (resolveInfo != null) {
                try {
                    final PackageInfo packageInfo = getApplicationContext().getPackageManager().getPackageInfo(resolveInfo.activityInfo.packageName, PackageManager.GET_SIGNATURES);
                    for (final Signature signature : packageInfo.signatures) {
                        if (signature.toCharsString().equals(ODKL_APP_SIGNATURE)) {
                            ssoAvailable = true;
                        }
                    }
                } catch (NameNotFoundException exc) {
                }
            }
            if (ssoAvailable) {
                success("true", callbackContext);
            } else {
                success("false", callbackContext);
            }
            return true;
        } else if(ACTION_PERFORM_POSTING.equals(action)) {
            JSONObject params = args.getJSONObject(0);
            return performPosting(params.getString("st.attachment"), callbackContext);
        } else if(ACTION_PERFORM_INVITE.equals(action)) {
            JSONObject params = args.getJSONObject(0);
            return performInvite(JsonHelper.toMap(params), callbackContext);
        } else if(ACTION_PERFORM_SUGGEST.equals(action)) {
            JSONObject params = args.getJSONObject(0);
            return performSuggest(JsonHelper.toMap(params), callbackContext);
        } else if(ACTION_REPORT_STATS.equals(action)) {
            JSONObject params = args.getJSONObject(0);
            return reportStats(JsonHelper.toMap(params), callbackContext);
        }
        Log.e(TAG, "Unknown action: "+action);
        fail("Unimplemented method: "+action, callbackContext);
        return true;
    }

    @Override public void onActivityResult(int requestCode, int resultCode, Intent data) 
    {
        Log.i(TAG, "onActivityResult "+requestCode+" "+resultCode+" "+(data!=null));
        if (data != null && odnoklassnikiObject.isActivityRequestOAuth(requestCode)) {
            odnoklassnikiObject.onAuthActivityResult(requestCode, resultCode, data, new OkListener() {
                    @Override
                    public void onSuccess(final JSONObject json) {
                        final String token = json.optString("access_token");
                        final String uid = json.optString("logged_in_user");
                        final String sessionSecretKey = json.optString("session_secret_key");
                        Log.i(TAG, "Odnoklassniki accessToken = " + token);
                        afterLogin(token, uid, sessionSecretKey, null);
                    }
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "OK login error: "+error);
                        fail("OK login error: "+error, null);
                        //Toast.makeText(webView.getContext(), "Ошибка во время авторизации в приложении через \"Одноклассников\".", Toast.LENGTH_LONG).show();
                    }
                });
        } else if (data != null && odnoklassnikiObject.isActivityRequestViral(requestCode)) {
            odnoklassnikiObject.onActivityResultResult(requestCode, resultCode, data, new OkListener() {
                    @Override
                    public void onSuccess(final JSONObject json) {
                        Log.i(TAG, "Operation completed: "+json.toString());
                        success(json.toString(), null);
                    }
                    @Override
                    public void onError(String error) {
                        Log.e(TAG, "Posting error:"+error);
                        //Toast.makeText(webView.getContext(), "Ошибка OK: "+error, Toast.LENGTH_LONG).show();
                        fail(error, null);
                    }
                });
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    private boolean init(String appId, String key, final CallbackContext context)
    {
        REDIRECT_URL = "okauth://ok" + appId;
        mAppId = appId;
        mAppKey = key;
        //odnoklassnikiObject = Odnoklassniki.createInstance(webView.getContext(), appId, key);
        odnoklassnikiObject = new Odnoklassniki(webView.getContext(), appId, key) {
            {
                allowWidgetRetry = false;
                sOdnoklassniki = this;
            }
        };
        success("ok", context);
        return true;
    }

    private void afterLogin(final String token, final String uid, final String sessionSecretKey, final CallbackContext callbackContext)
    {
        new AsyncTask<String, Void, String>() {
            @Override protected String doInBackground(String... args) {
                try {
                    return odnoklassnikiObject.request("users.getCurrentUser", null, null);
                } catch (IOException e) {
                    e.printStackTrace();
                    fail("OK login error:" + e, callbackContext);
                }
                return null;
            }
            @Override protected void onPostExecute(String result) {
                try {
                    JSONObject loginDetails = new JSONObject();
                    loginDetails.put("token", token);
                    loginDetails.put("user", new JSONObject(result));
                    loginDetails.put("session_secret_key", sessionSecretKey);
                    success(loginDetails.toString(), callbackContext);
                    Log.i(TAG, "Login details:"+loginDetails.toString());
                } catch (Exception e) {
                    String err = "OK login error: " + e;
                    Log.e(TAG, err);
                    fail(err, callbackContext);
                }
            }
        }.execute();
    }
    
    private boolean login(final JSONArray permissions, final CallbackContext context) 
    {
        final SocialOk self = this;
        lastLoginPermissions = permissions;
        odnoklassnikiObject.checkValidTokens(new OkListener() {
                @Override
                public void onSuccess(JSONObject json) {
                    //Log.i(TAG, "Token valid: "+json.toString());
                    final String token = json.optString("access_token");
                    final String uid = json.optString("logged_in_user");
                    final String sessionSecretKey = json.optString("session_secret_key");
                    Log.i(TAG, "Odnoklassniki accessToken = " + token);
                    afterLogin(token, uid, sessionSecretKey, context);
                }
                @Override
                public void onError(String error) {
                    //Toast.makeText(MainActivity.this, String.format("%s: %s", getString(R.string.error), error), Toast.LENGTH_LONG).show();
                    Log.e(TAG, "Token invalid. "+ error);
                    odnoklassnikiObject.clearTokens();
                    //login(lastLoginPermissions, _callbackContext);
                    //вызываем запрос авторизации. После OAuth будет вызван callback, определенный для объекта
                    String[] perm;
                    if(permissions != null && permissions.length() > 0) {
                        perm = new String[permissions.length()];
                        for(int i=0; i<permissions.length(); i++) {
                            perm[i] = permissions.optString(i, "");
                        }
                    } else {
                        perm = new String[1];
                        perm[0] = OkScope.VALUABLE_ACCESS;
                    }
                    self._callbackContext = context;
                    OkAuthType authType = OkAuthType.ANY;
                    odnoklassnikiObject.requestAuthorization(getActivity(), REDIRECT_URL, authType, perm);
                    /*
                    final Intent intent = new Intent(getActivity(), OkAuthActivity.class);
                    intent.putExtra(Shared.PARAM_CLIENT_ID, mAppId);
                    intent.putExtra(Shared.PARAM_APP_KEY, mAppKey);
                    intent.putExtra(Shared.PARAM_REDIRECT_URI, REDIRECT_URL);
                    intent.putExtra(Shared.PARAM_AUTH_TYPE, authType);
                    intent.putExtra(Shared.PARAM_SCOPES, perm);
                    cordova.startActivityForResult(self, intent, Shared.OK_AUTH_REQUEST_CODE);
                    */
                    Log.i(TAG, "Login requested with permissions:" + perm.toString());
                    self.cordova.setActivityResultCallback(self);
                }
            });

        return true;
    }

    private boolean logout(final CallbackContext callbackContext) 
    {
        odnoklassnikiObject.clearTokens();
        success("Ok", callbackContext);
        return true;
    }

    private boolean share(final String url, final String comment, final CallbackContext callbackContext)
    {
        final Map<String, String> params = new HashMap<String, String>();
        params.put("linkUrl", url);
        params.put("comment", comment);
        new AsyncTask<String, Void, String>() {
            @Override protected String doInBackground(String... args) {
                try {
                    return odnoklassnikiObject.request("share.addLink", params, null);
                } catch (IOException e) {
                    e.printStackTrace();
                    fail("Error", callbackContext);
                }
                return null;
            }
            @Override protected void onPostExecute(String result) {
                Log.i(TAG, "OK share result" + result);
                success(result, callbackContext);
            }
        }.execute();
        return true;
    }

    private boolean friendsGet(final String fid, final String sort_type, final CallbackContext context)
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("fid", fid);
        params.put("sort_type", sort_type);
        return callApiMethod("friends.get", params, context);
    }

    private boolean friendsGetOnline(final String uid, final String online, final CallbackContext context)
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("uid", uid);
        params.put("online", online);
        return callApiMethod("friends.getOnline", params, context);
    }

    private boolean usersGetInfo(final String uids, final String fields, final CallbackContext context)
    {
        Map<String, String> params = new HashMap<String, String>();
        params.put("uids", uids);
        params.put("fields", fields);
        return callApiMethod("users.getInfo", params, context);
    }

    private boolean callApiMethod(final String method, final Map<String, String> params, final CallbackContext context) 
    {
        new AsyncTask<String, Void, String>() {
            @Override protected String doInBackground(String... args) {
                try {
                    return odnoklassnikiObject.request(method, params, null);
                } catch (Exception e) {
                    fail(e.toString(), context);
                }
                return null;
            }
            @Override protected void onPostExecute(String result) {
                success(result, context);
            }
        }.execute();
        return true;
    }

    private boolean getApiMethod(final String method, final Map<String, String> params, final CallbackContext context) 
    {
        new AsyncTask<String, Void, String>() {
            @Override protected String doInBackground(String... args) {
                try {
                    return odnoklassnikiObject.request(method, params, EnumSet.of(OkRequestMode.UNSIGNED));
                } catch (Exception e) {
                    fail(e.toString(), context);
                }
                return null;
            }
            @Override protected void onPostExecute(String result) {
                success(result, context);
            }
        }.execute();
        return true;
    }

    private boolean performPosting(String attachment, final CallbackContext context)
    {
        Log.w(TAG, "Posting attachment: "+attachment);
        this.cordova.setActivityResultCallback(this);
        this._callbackContext = context;
        odnoklassnikiObject.performPosting(getActivity(), attachment, true, null);
        return true;
    }

    private boolean performSuggest(HashMap<String, String> params, final CallbackContext context)
    {
        this.cordova.setActivityResultCallback(this);
        this._callbackContext = context;
        odnoklassnikiObject.performAppSuggest(getActivity(), params);
        return true;
    }

    private boolean reportStats(HashMap<String, String> params, final CallbackContext context)
    {
        return getApiMethod("sdk.reportStats", params, context);
    }

    private boolean performInvite(HashMap<String, String> params, final CallbackContext context)
    {
        this.cordova.setActivityResultCallback(this);
        this._callbackContext = context;
        odnoklassnikiObject.performAppInvite(getActivity(), params);
        return true;
    }
}


