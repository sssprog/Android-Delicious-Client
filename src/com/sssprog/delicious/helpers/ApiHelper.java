package com.sssprog.delicious.helpers;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.text.TextUtils;
import android.widget.Toast;

import com.sssprog.delicious.App;
import com.sssprog.delicious.R;
import com.sssprog.delicious.api.ApiResult.ApiRestRequestStatus;

public class ApiHelper {
	
	@SuppressWarnings("incomplete-switch")
	public static void showError(ApiRestRequestStatus status) {
		switch (status) {
		case RestStatusNetworkDown:
			Toast.makeText(App.getContext(), R.string.response_error_network_down, Toast.LENGTH_LONG).show();
			break;
			
		case RestStatusServerError:
		case RestStatusServerOrNetworkError:
		case RestStatusThrottled:
			Toast.makeText(App.getContext(), R.string.response_error_standard, Toast.LENGTH_LONG).show();
			break;
			
		}
	}
	
	public static Tuple2<Set<String>, String> tagsToList(String s) {
		String ptags = "";
		Set<String> tags = new HashSet<String>();
		if (s != null) {
			String[] atags = s.split(" ");
			for (String t: atags) {
				t = t.trim().toLowerCase();
				if (TextUtils.isEmpty(t))
					continue;
				if (!tags.contains(t))
					ptags += t + " ";
				tags.add(t);
			}
			ptags = ptags.trim();
		}
		
		return Tuple2.newInstance(tags, ptags);
	}

}
