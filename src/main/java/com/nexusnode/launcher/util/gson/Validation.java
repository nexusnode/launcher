package com.nexusnode.launcher.util.gson;

import com.google.gson.JsonParseException;

/**
 * Check if the json object's fields automatically filled by Gson are in right format.
 *
 * @author huangyuhui
 */
public interface Validation {

    /**
     * 1. Check some non-null fields and;
     * 2. Check strings and;
     * 3. Check generic type of lists <T> and maps <K, V> are correct.
     *
     * Will be called immediately after initialization.
     * Throw an exception when values are malformed.
     *
     * @throws JsonParseException if fields are filled in wrong format or wrong type.
     * @throws TolerableValidationException if we want to replace this object with null (i.e. the object does not fulfill the constraints).
     */
    void validate() throws JsonParseException;//, TolerableValidationException;
}

