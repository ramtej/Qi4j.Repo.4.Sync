/*  Copyright 2008 Edward Yakop.
*
* Licensed under the Apache License, Version 2.0 (the "License");
* you may not use this file except in compliance with the License.
* You may obtain a copy of the License at
*
* http://www.apache.org/licenses/LICENSE-2.0
*
* Unless required by applicable law or agreed to in writing, software
* distributed under the License is distributed on an "AS IS" BASIS,
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or
* implied.
*
* See the License for the specific language governing permissions and
* limitations under the License.
*/
package org.qi4j.library.swing.visualizer.detailPanel.internal.common.form;

import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.JLabel;
import javax.swing.JComponent;
import org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor;
import org.qi4j.spi.composite.CompositeDescriptor;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;

/**
 * @author edward.yakop@gmail.com
 * @see org.qi4j.library.swing.visualizer.model.CompositeDetailDescriptor
 * @since 0.5
 */
public class CompositeDescriptorForm extends JPanel
{
    private JPanel form;

    private JTextField compositeClassName;
    private JTextField compositeURI;
    private JTextField compositeVisibility;

    public final void updateModel( CompositeDetailDescriptor aDescriptor )
    {
        String className = null;
        String uri = null;
        String visibility = null;

        if( aDescriptor != null )
        {
            CompositeDescriptor descriptor = aDescriptor.descriptor();
            className = descriptor.type().getName();
            uri = descriptor.toURI();
            visibility = descriptor.visibility().toString();
        }

        compositeClassName.setText( className );
        compositeURI.setText( uri );
        compositeVisibility.setText( visibility );
    }

    private void createUIComponents()
    {
        form = this;
    }

    {
// GUI initializer generated by IntelliJ IDEA GUI Designer
// >>> IMPORTANT!! <<<
// DO NOT EDIT OR ADD ANY CODE HERE!
        $$$setupUI$$$();
    }

    /**
     * Method generated by IntelliJ IDEA GUI Designer
     * >>> IMPORTANT!! <<<
     * DO NOT edit this method OR call it in your code!
     *
     * @noinspection ALL
     */
    private void $$$setupUI$$$()
    {
        createUIComponents();
        form.setLayout( new FormLayout( "fill:max(d;4px):noGrow,left:m:noGrow,left:4dlu:noGrow,fill:d:grow", "center:max(d;4px):noGrow,center:d:noGrow,top:4dlu:noGrow,center:p:noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow,center:max(d;4px):noGrow,top:4dlu:noGrow" ) );
        final JLabel label1 = new JLabel();
        label1.setText( "Composite" );
        CellConstraints cc = new CellConstraints();
        form.add( label1, cc.xy( 2, 2 ) );
        final JLabel label2 = new JLabel();
        label2.setText( "Class name" );
        form.add( label2, cc.xy( 2, 4 ) );
        final JLabel label3 = new JLabel();
        label3.setText( "URI" );
        form.add( label3, cc.xy( 2, 6 ) );
        final JLabel label4 = new JLabel();
        label4.setText( "Visibility" );
        form.add( label4, cc.xy( 2, 8 ) );
        compositeClassName = new JTextField();
        form.add( compositeClassName, cc.xy( 4, 4, CellConstraints.FILL, CellConstraints.DEFAULT ) );
        compositeURI = new JTextField();
        form.add( compositeURI, cc.xy( 4, 6, CellConstraints.FILL, CellConstraints.DEFAULT ) );
        compositeVisibility = new JTextField();
        form.add( compositeVisibility, cc.xy( 4, 8, CellConstraints.FILL, CellConstraints.DEFAULT ) );
    }

    /**
     * @noinspection ALL
     */
    public JComponent $$$getRootComponent$$$()
    {
        return form;
    }
}
