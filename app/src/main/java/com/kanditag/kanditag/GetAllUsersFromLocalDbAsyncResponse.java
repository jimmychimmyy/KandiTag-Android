package com.kanditag.kanditag;

import java.util.ArrayList;

/**
 * Created by Jim on 3/6/15.
 */
public interface GetAllUsersFromLocalDbAsyncResponse {
    void processFinish(ArrayList<KtUserObjectParcelable> output);
}
