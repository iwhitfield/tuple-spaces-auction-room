package com.zackehh.javaspaces.ui.components;

import javax.swing.*;

/**
 * Created by iwhitfield on 01/12/14.
 */
public class JResultText extends JLabel {

    @Override
    public void setText(String text){
        super.setText("  " + text);
    }

}