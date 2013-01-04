package com.sssprog.delicious.api;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.StatusLine;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.params.ClientPNames;
import org.apache.http.client.params.CookiePolicy;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.content.LocalBroadcastManager;
import android.text.TextUtils;
import android.util.Base64;

import com.google.inject.Singleton;
import com.sssprog.activerecord.Database;
import com.sssprog.delicious.App;
import com.sssprog.delicious.api.ApiResult.ApiRestRequestStatus;
import com.sssprog.delicious.dbmodels.PostModel;
import com.sssprog.delicious.dbmodels.TagModel;
import com.sssprog.delicious.dbmodels.TagPostModel;
import com.sssprog.delicious.helpers.ApiHelper;
import com.sssprog.delicious.helpers.LogHelper;
import com.sssprog.delicious.helpers.Prefs;
import com.sssprog.delicious.helpers.Tuple2;

@Singleton
public class ApiImpl implements Api {
	
	private static final String LOG_TAG_SERVER = "server";
	
	private final DeliciousClient mClient = new DeliciousClient();
	
	private static class ApiAsyncTask2<Params, Progress, Result> extends ApiAsyncTask<Params, Progress, Result> {
		@Override
		protected Result doInBackground(Params... params) {
			while (!Database.isOpen()) {
				try {
					Thread.sleep(100);
				} catch (InterruptedException e) {
				}
			}
			return null;
		}
	}
	
	private static final class RestRequest {
		public String url;
		public String userName;
		public String password;
		public List<NameValuePair> params;
		
		public RestRequest(String url) {
			userName = Prefs.getString(Prefs.USER_NAME);
			password = Prefs.getString(Prefs.PASSWORD);
			this.url = url;
		}
	}
	
	private static final class RestResponse {
		public HttpResponse response;
		public ApiRestRequestStatus status = null;
		public String content;
		
		public InputStream getContent() {
			if (content == null)
				return null;
			try {
				return new ByteArrayInputStream(content.getBytes("UTF-8"));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	private static boolean hasNetworkConnection() {
		ConnectivityManager cm = (ConnectivityManager) App.getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
		NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
		boolean isConnected = false;
		if (activeNetwork != null)
			isConnected = activeNetwork.isConnected();
		return isConnected;
	}
	
	private static final class DeliciousClient {
		private static final int MIN_REQUESTS_INTERVAL = 1000;
		private static final int MAX_REQUESTS_INTERVAL = 30000;
		private static final List<Integer> BACK_OFF_CODES = Arrays.asList(500, 999);
		private int requestInterval = MIN_REQUESTS_INTERVAL;
		private DefaultHttpClient httpclient;
		private volatile long mLastRequestFinishedTime = 0;
		
		public DeliciousClient() {
			httpclient = new DefaultHttpClient();
			HttpParams params = httpclient.getParams();
			// Set the timeout in milliseconds until a connection is established.
			HttpConnectionParams.setConnectionTimeout(params, 20000);
			// Set the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data.
			HttpConnectionParams.setSoTimeout(params, 20000);
			params.setParameter(ClientPNames.COOKIE_POLICY,
			        CookiePolicy.BROWSER_COMPATIBILITY);
		}
		
		public synchronized RestResponse execute(RestRequest request) {
			RestResponse res = new RestResponse();
			long interval = System.currentTimeMillis() - mLastRequestFinishedTime;
			if (interval < requestInterval) {
				// Wait current interval between requests
				LogHelper.d(LOG_TAG_SERVER, "wait before execute request " + (requestInterval - interval) + " milis");
				try {
					Thread.sleep(requestInterval - interval);
				} catch (InterruptedException e) {
				}
			}
			// Network down
			if (!hasNetworkConnection()) {
				res.status = ApiRestRequestStatus.RestStatusNetworkDown;
				return res;
			}
			// Make a request
			String url = request.url;
			if (request.params != null) {
				String params = "";
				for (NameValuePair p: request.params) {
					try {
						params += "&" + p.getName() + "=" + URLEncoder.encode(p.getValue(), "UTF-8");
					} catch (UnsupportedEncodingException e) {
						e.printStackTrace();
					}
				}
				if (!TextUtils.isEmpty(params))
					params = params.substring(1);
				url += "?" + params;
			}
			HttpGet httpget = new HttpGet(url);
			httpget.setHeader("User-Agent", "android delicious client");
			String auth = request.userName + ":" + request.password;
			try {
				httpget.addHeader("Authorization", "Basic " + Base64.encodeToString(auth.getBytes("UTF-8"), Base64.DEFAULT));
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}
			try {
				long start = System.currentTimeMillis();
				HttpResponse response = httpclient.execute(httpget);
				res.response = response;
				float time = (System.currentTimeMillis() -start) / 1000f;
				StatusLine s = response.getStatusLine();
				String content = null;
				if (response.getEntity() != null) {
					BufferedReader in = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), "UTF-8"));
					String line = in.readLine();
					StringBuilder sb = new StringBuilder();
					while (line != null) {
						sb.append(line).append('\n');
						line = in.readLine();
					}
					content = sb.toString();
					res.content = content;
				}
				if (s != null) {
					if (s.getStatusCode() == 200)
						LogHelper.i(LOG_TAG_SERVER, "server success " + "  " + String.format("%.2fs", time) + 
								", Url = " + url + "\nresponse status = " + s + "\nresponse = " + content);
					else
						LogHelper.d(LOG_TAG_SERVER, "server error " + String.format("%.2fs", time) + 
								", Url = " + url + "\nresponse status = " + s + "\nresponse = " + content);
				}
				if (s == null) {
					res.status = ApiRestRequestStatus.RestStatusServerOrNetworkError;
					return res;
				}
				if (BACK_OFF_CODES.contains(s.getStatusCode())) { // Throttled
					requestInterval *= 2;
					if (requestInterval > MAX_REQUESTS_INTERVAL)
						requestInterval = MAX_REQUESTS_INTERVAL;
					res.status = ApiRestRequestStatus.RestStatusThrottled;
					return res;
				}
				requestInterval /= 2;
				if (requestInterval < MIN_REQUESTS_INTERVAL)
					requestInterval = MIN_REQUESTS_INTERVAL;
				if (s.getStatusCode() == 401) {
					res.status = ApiRestRequestStatus.RestStatusAuthFailed;
					return res;
				}
				return res;
			} catch (ClientProtocolException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			res.status = ApiRestRequestStatus.RestStatusServerOrNetworkError;
			return res;
		}
		
	}
	
	/* *********************************************************************
	 * Auth request
	 */
	private final AuthStatus mAuthStatus = new AuthStatus();
	private static class AuthStatus {
		public boolean inProgress = false;
	}
	private static final String AUTH_URL = "https://api.del.icio.us/v1/posts/update";
	
	@Override
	public void authenticate(final String userName, final String password, final ApiCallback<Void> apiCallback) {
		if (mAuthStatus.inProgress)
			throw new IllegalStateException("auth request is already running");
		mAuthStatus.inProgress = true;
		new ApiAsyncTask2<Void, Void, ApiResult<Void>>() {
			ApiResult<Void> result;

			@Override
			protected ApiResult<Void> doInBackground(Void... params) {
				super.doInBackground(params);
				RestRequest r = new RestRequest(AUTH_URL);
				r.userName = userName;
				r.password = password;
				RestResponse rr = mClient.execute(r);
				if (rr.status != null) {
					return ApiResult.newInstance(rr.status, null);
				}
				if (rr.response.getStatusLine().getStatusCode() == 200) {
					result = ApiResult.newInstance(ApiRestRequestStatus.RestStatusOk, null);
					Prefs.setBoolean(Prefs.AUTHENTICATED, true);
					Prefs.setString(Prefs.USER_NAME, userName);
					Prefs.setString(Prefs.PASSWORD, password);
				} else
					result = ApiResult.newInstance(ApiRestRequestStatus.RestStatusServerError, null);
				
				return result;
			}
			
			protected void onPostExecute(ApiResult<Void> result) {
				mAuthStatus.inProgress = false;
				if (isAuthenticated())
					sync();
				if (apiCallback != null)
					apiCallback.onApiResult(result);
			}
			
		}.execute();
	}

	@Override
	public boolean authIsRunning() {
		return mAuthStatus.inProgress;
	}
	
	/* *********************************************************************
	 * Syncronization
	 */
	private final SyncStatus mSyncStatus = new SyncStatus();
	private static class SyncStatus {
		public volatile boolean inProgress = false;
	}
	private static final String SYNC_URL = "https://api.del.icio.us/v1/posts/all";
	
	private void sendBroadcastSyncStatusChanged(boolean syncing) {
		Intent intent = new Intent(BROADCAST_ACTION_SYNC_STATUS_CHANGED);
		intent.putExtra(BROADCAST_SYNC_STATUS_CHANGED_PARAM_SYNCING, syncing);
		LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
	}
	
	private static class SyncSaxHandler extends DefaultHandler {
		private Map<String, TagModel> tagsMap = new HashMap<String, TagModel>();
		DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
		{
			df.setTimeZone(TimeZone.getTimeZone("UTC"));
		}
		
		private boolean getBooleanValue(Attributes attributes, String name) {
			String v = attributes.getValue(name);
			return "yes".equalsIgnoreCase(v);
		}
		
		@SuppressLint("SimpleDateFormat")
		private long getDateValue(Attributes attributes, String name) {
			String v = attributes.getValue(name);
			if (v == null)
				return 0;
			v = v.replaceAll("[zZ]", "");
			try {
				Date d = df.parse(v);
				return d.getTime();
			} catch (ParseException e) {
				return 0;
			}
		}
		
		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (localName.equalsIgnoreCase("post")) {
				Tuple2<Set<String>, String> tr = ApiHelper.tagsToList(attributes.getValue("tag"));
				Set<String> tags = tr.value1;
				String ptags = tr.value2;
				
				PostModel post = new PostModel();
				post.description = attributes.getValue("description");
				post.extended = attributes.getValue("extended");
				post.href = attributes.getValue("href");
				post.privatePost = getBooleanValue(attributes, "private");
				post.creationDate = getDateValue(attributes, "time");
				post.tags = ptags;
				post.save();

				for (String t: tags) {
					if (TextUtils.isEmpty(t))
						continue;
					TagModel tag = tagsMap.get(t);
					if (tag == null) {
						tag = new TagModel();
						tagsMap.put(t, tag);
						tag.name = t;
						tag.count = 0;
					}
					tag.count++;
					tag.save();

					TagPostModel tp = new TagPostModel();
					tp.postId = post.getID();
					tp.tagId = tag.getID();
					tp.save();
				}
			}
		}
		
	}
	
	@Override
	public void sync() {
		if (mSyncStatus.inProgress)
			return;
		mSyncStatus.inProgress = true;
		sendBroadcastSyncStatusChanged(true);
		new ApiAsyncTask2<Void, Void, Void>() {

			@Override
			protected Void doInBackground(Void... params) {
				super.doInBackground(params);
				RestRequest r = new RestRequest(SYNC_URL);
				RestResponse rr = mClient.execute(r);
				if (rr.status != null || rr.response.getStatusLine().getStatusCode() != 200) {
					return null;
				}
				
				Database.beginTransaction();
				Database.delete(PostModel.class, null, null);
				Database.delete(TagPostModel.class, null, null);
				Database.delete(TagModel.class, null, null);
				SAXParserFactory factory = SAXParserFactory.newInstance();
				try {
					SAXParser saxParser = factory.newSAXParser();
					saxParser.parse(new InputSource(rr.getContent()), new SyncSaxHandler());
					Database.setTransactionSuccessful();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (IllegalStateException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
				Database.endTransaction();
				return null;
			}
			
			protected void onPostExecute(Void result) {
				mSyncStatus.inProgress = false;
				sendBroadcastSyncStatusChanged(false);
			}
			
		}.execute();
	}
	
	@Override
	public boolean isAuthenticated() {
		return Prefs.getBoolean(Prefs.AUTHENTICATED);
	}
	
	@Override
	public boolean isSyncing() {
		return mSyncStatus.inProgress;
	}

	/* *********************************************************************
	 * Save request
	 */
	private static final String SAVE_URL = "https://api.del.icio.us/v1/posts/add";
	
	@Override
	public void savePost(final PostModel post, final ApiCallback<Void> apiCallback) {
		new ApiAsyncTask2<Void, Void, ApiResult<Void>>() {
			
			protected ApiResult<Void> doInBackground(Void... params) {
				Set<String> tags = ApiHelper.tagsToList(post.tags).value1;
				RestRequest r = new RestRequest(SAVE_URL);
				List<NameValuePair> p = new ArrayList<NameValuePair>();
				p.add(new BasicNameValuePair("url", post.href));
				p.add(new BasicNameValuePair("description", post.description));
				if (!TextUtils.isEmpty(post.extended))
					p.add(new BasicNameValuePair("extended", post.extended));
				p.add(new BasicNameValuePair("tags", TextUtils.join(",", tags)));
				p.add(new BasicNameValuePair("replace", "yes"));
				p.add(new BasicNameValuePair("shared", post.privatePost ? "no" : "yes"));
				r.params = p;
				
				
				RestResponse rr = mClient.execute(r);
				if (rr.status != null) {
					return ApiResult.newInstance(rr.status, null);
				} else if (rr.response.getStatusLine().getStatusCode() == 200) {
					Database.beginTransaction();
					if (post.getID() > 0) {
						deleteTagsForPost(post);
					}
					post.save();
					for (String t: tags) {
						List<TagModel> list = Database.findByColumnValue(TagModel.class, "name", t).getAll();
						TagModel tag;
						if (list.isEmpty()) {
							tag = new TagModel();
							tag.name = t;
							tag.count = 0;
						} else
							tag = list.get(0);
						tag.count++;
						tag.save();
						TagPostModel tp = new TagPostModel();
						tp.postId = post.getID();
						tp.tagId = tag.getID();
						tp.save();
					}
					
					Database.setTransactionSuccessful();
					Database.endTransaction();
					sendBroadcastDbDataChanged();
					return ApiResult.newInstance(ApiRestRequestStatus.RestStatusOk, null);
				} else
					return ApiResult.newInstance(ApiRestRequestStatus.RestStatusServerError, null);
			}
			
			protected void onPostExecute(ApiResult<Void> result) {
				if (apiCallback != null)
					apiCallback.onApiResult(result);
			}
			
		}.execute();
	}
	
	private void sendBroadcastDbDataChanged() {
		Intent intent = new Intent(BROADCAST_ACTION_DB_DATA_CHANGED);
		LocalBroadcastManager.getInstance(App.getContext()).sendBroadcast(intent);
	}
	
	private void deleteTagsForPost(PostModel post) {
		List<TagPostModel> tps = Database.findByColumnValue(TagPostModel.class, "postId", "" + post.getID()).getAll();
		for (TagPostModel tp: tps) {
			TagModel t = Database.findById(TagModel.class, tp.tagId);
			t.count--;
			if (t.count == 0)
				t.delete();
			else
				t.save();
			tp.delete();
		}
	}
	
	/* *********************************************************************
	 * Delete request
	 */
	private static final String DELETE_POST_URL = "https://api.del.icio.us/v1/posts/delete";
	
	@Override
	public void deletePost(final PostModel post, final ApiCallback<Void> apiCallback) {
		new ApiAsyncTask2<Void, Void, ApiResult<Void>>() {
			
			protected ApiResult<Void> doInBackground(Void... params) {
				RestRequest r = new RestRequest(DELETE_POST_URL);
				List<NameValuePair> p = new ArrayList<NameValuePair>();
				p.add(new BasicNameValuePair("url", post.href));
				r.params = p;
				
				
				RestResponse rr = mClient.execute(r);
				if (rr.status != null) {
					return ApiResult.newInstance(rr.status, null);
				} else if (rr.response.getStatusLine().getStatusCode() == 200) {
					Database.beginTransaction();
					deleteTagsForPost(post);
					post.delete();
					
					Database.setTransactionSuccessful();
					Database.endTransaction();
					sendBroadcastDbDataChanged();
					return ApiResult.newInstance(ApiRestRequestStatus.RestStatusOk, null);
				} else
					return ApiResult.newInstance(ApiRestRequestStatus.RestStatusServerError, null);
			}
			
			protected void onPostExecute(ApiResult<Void> result) {
				if (apiCallback != null)
					apiCallback.onApiResult(result);
			}
			
		}.execute();
	}
	
	public void logout() {
		Prefs.remove(Prefs.USER_NAME);
		Prefs.remove(Prefs.PASSWORD);
		Prefs.remove(Prefs.AUTHENTICATED);
	}

}
