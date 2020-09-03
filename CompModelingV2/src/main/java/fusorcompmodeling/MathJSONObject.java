/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package fusorcompmodeling;

import matheval.MathEval;
import org.json.JSONObject;

/**
 *
 * @author guberti
 */
public class MathJSONObject extends JSONObject{
    /**
     *
     * @param src
     */
    public MathJSONObject() {
        super();
    }
    public MathJSONObject(JSONObject src) {
        super(src.toString());
    }

    public MathJSONObject(String json) {
        super(json);
    }
    public double getMath(String key) {
        MathEval eval = new MathEval();
        return eval.evaluate(super.getString(key));
    }
}
