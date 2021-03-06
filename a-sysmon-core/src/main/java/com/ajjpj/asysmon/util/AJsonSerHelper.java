package com.ajjpj.asysmon.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.nio.charset.Charset;


/**
 * This class is a collection of helper methods for manually writing JSON to an OutputStream. <p />
 *
 * For details on the JSON spec, see http://json.org
 *
 * @author arno
 */
public class AJsonSerHelper {
    final Charset UTF_8 = Charset.forName("UTF-8");

    private final int[] TEN_POW = new int[] {1, 10, 100, 1000, 10*1000, 100*1000, 1000*1000, 10*1000*1000, 100*1000*1000, 1000*1000*1000};

    private final Writer out;
    private final ArrayStack<JsonSerState> state = new ArrayStack<JsonSerState>();

    public AJsonSerHelper(OutputStream out) {
        this.out = new OutputStreamWriter(out, UTF_8);
        state.push(JsonSerState.initial);
    }

    public void startObject() throws IOException {
        checkAcceptsValueAndPrefixComma();
        state.push(JsonSerState.startOfObject);
        out.write("{");
    }

    public void endObject() throws IOException {
        checkInObject();
        state.pop();
        out.write("}");
        afterValueWritten();
    }

    public void writeKey(String key) throws IOException {
        if(!state().acceptsKey) {
            throw new IllegalStateException("state " + state() + " does not accept a key");
        }
        if(state() == JsonSerState.inObject) {
            out.write(",");
        }
        _writeStringLiteral(key);
        out.write(":");
        state.push(JsonSerState.afterKey);
    }

    public void startArray() throws IOException {
        checkAcceptsValueAndPrefixComma();
        state.push(JsonSerState.startOfArray);
        out.write("[");
    }

    public void endArray() throws IOException {
        checkInArray();
        state.pop();
        out.write("]");
        afterValueWritten();
    }

    public void writeStringLiteral(String s) throws IOException {
        checkAcceptsValueAndPrefixComma();
        if(s == null) {
            writeNullLiteral();
        }
        else {
            _writeStringLiteral(s);
        }
        afterValueWritten();
    }

    private void _writeStringLiteral(String s) throws IOException {
        out.write('"');
        for(int i=0; i<s.length(); i++) {
            final char ch = s.charAt(i);

            if(ch == '"') {
                out.write("\\\"");
            }
            else if (ch == '\\') {
                out.write ("\\\\");
            }
            else if(ch < 16) {
                out.write("\\u000" + Integer.toHexString(ch));
            }
            else if(ch < 32) {
                out.write("\\u00" + Integer.toHexString(ch));
            }
            else {
                out.write(ch);
            }
        }

        out.write('"');
    }

    public void writeNumberLiteral(long value, int numFracDigits) throws IOException {
        checkAcceptsValueAndPrefixComma();

        if(numFracDigits == 0) {
            out.write(String.valueOf(value));
        }
        else {
            final long intPart = value / TEN_POW[numFracDigits];
            final String fracPart = String.valueOf(1000*1000*1000 + value%TEN_POW[numFracDigits]).substring(10-numFracDigits, 10);

            out.write(String.valueOf(intPart));
            out.write(".");
            out.write(fracPart);
        }

        afterValueWritten();
    }

    public void writeBooleanLiteral(boolean value) throws IOException {
        checkAcceptsValueAndPrefixComma();
        out.write(String.valueOf(value));
        afterValueWritten();
    }

    public void writeNullLiteral() throws IOException {
        checkAcceptsValueAndPrefixComma();
        out.write("null");
        afterValueWritten();
    }

    //----------------------------------------- helper methods

    private void checkAcceptsValueAndPrefixComma() throws IOException {
        if(!state().acceptsValue) {
            throw new IllegalStateException("state " + state() + " does not accept a value");
        }
        if(state() == JsonSerState.inArray) {
            out.write(",");
        }
    }

    private void afterValueWritten() throws IOException {
        if(state() == JsonSerState.afterKey) {
            state.pop();
        }
        switch(state()) {
            case startOfArray:  replaceState(JsonSerState.inArray); break;
            case startOfObject: replaceState(JsonSerState.inObject); break;
            case initial:
                replaceState(JsonSerState.finished);
                out.flush();
                break;
            default:
        }
    }

    private void checkInObject() {
        if(state() != JsonSerState.inObject && state() != JsonSerState.startOfObject) {
            throw new IllegalStateException("not in an object");
        }
    }

    private void checkInArray() {
        if(state() != JsonSerState.inArray && state() != JsonSerState.startOfArray) {
            throw new IllegalStateException("not in an array");
        }
    }

    private void replaceState(JsonSerState newState) {
        state.pop();
        state.push(newState);
    }

    private JsonSerState state() {
        return state.peek();
    }
}

