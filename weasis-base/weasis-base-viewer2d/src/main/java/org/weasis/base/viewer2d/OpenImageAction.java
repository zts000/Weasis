/*******************************************************************************
 * Copyright (c) 2010 Nicolas Roduit.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     Nicolas Roduit - initial API and implementation
 ******************************************************************************/
package org.weasis.base.viewer2d;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.weasis.core.api.gui.util.FileFormatFilter;
import org.weasis.core.api.media.MimeInspector;
import org.weasis.core.api.media.data.Codec;
import org.weasis.core.api.media.data.MediaElement;
import org.weasis.core.api.media.data.MediaReader;
import org.weasis.core.api.media.data.MediaSeries;
import org.weasis.core.api.service.BundleTools;
import org.weasis.core.ui.editor.ViewerPluginBuilder;
import org.weasis.core.ui.util.AbstractUIAction;

public class OpenImageAction extends AbstractUIAction {

    /** The singleton instance of this singleton class. */
    private static OpenImageAction openAction = null;

    /** Return the singleton instance */
    public static OpenImageAction getInstance() {
        if (openAction == null) {
            openAction = new OpenImageAction();
        }
        return openAction;
    }

    private OpenImageAction() {
        super("Image");
        setDescription("Open image files");
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        String directory = BundleTools.LOCAL_PERSISTENCE.getProperty("last.open.image.dir", "");//$NON-NLS-1$
        JFileChooser fileChooser = new JFileChooser(directory);

        fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        fileChooser.setMultiSelectionEnabled(true);
        // TODO add format from plugins
        FileFormatFilter.setImageDecodeFilters(fileChooser);
        File[] selectedFiles = null;
        if (fileChooser.showOpenDialog(EventManager.getInstance().getSelectedView2dContainer()) != JFileChooser.APPROVE_OPTION
            || (selectedFiles = fileChooser.getSelectedFiles()) == null) {
            return;
        } else {
            MediaSeries series = null;
            for (File file : selectedFiles) {
                String mimeType = MimeInspector.getMimeType(file);
                if (mimeType != null && mimeType.startsWith("image")) {
                    Codec codec = BundleTools.getCodec(mimeType, null);
                    if (codec != null) {
                        MediaReader reader = codec.getMediaIO(file.toURI(), mimeType, null);
                        if (reader != null) {
                            if (series == null) {
                                // TODO improve group model for image, uid for group ?
                                series = reader.getMediaSeries();
                            } else {
                                MediaElement[] elements = reader.getMediaElement();
                                if (elements != null) {
                                    for (MediaElement media : elements) {
                                        series.addMedia(media);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (series != null && series.size(null) > 1) {
                ViewerPluginBuilder.openSequenceInDefaultPlugin(series, ViewerPluginBuilder.DefaultDataModel, true,
                    false);
            } else {
                Component c = e.getSource() instanceof Component ? (Component) e.getSource() : null;
                JOptionPane.showMessageDialog(c, "Cannot open the requested files!", getDescription(),
                    JOptionPane.WARNING_MESSAGE);
            }
            BundleTools.LOCAL_PERSISTENCE.setProperty("last.open.image.dir", selectedFiles[0].getParent());
        }
    }
}
