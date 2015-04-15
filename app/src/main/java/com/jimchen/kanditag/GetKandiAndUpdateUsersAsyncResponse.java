package com.jimchen.kanditag;

import java.util.ArrayList;

/**
 * Created by Jim on 3/1/15.
 */
public interface GetKandiAndUpdateUsersAsyncResponse {
    void processFinish(ArrayList<KtUserObject> output);
}
