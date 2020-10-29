import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.printing.PDFPrintable;
import org.apache.pdfbox.printing.Scaling;

import javax.print.*;
import javax.print.attribute.HashDocAttributeSet;
import javax.print.attribute.HashPrintRequestAttributeSet;
import javax.print.attribute.standard.Sides;
import javax.swing.*;
import javax.swing.text.DefaultStyledDocument;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.print.Book;
import java.awt.print.PageFormat;
import java.awt.print.Paper;
import java.awt.print.PrinterJob;
import java.io.*;
import java.util.Date;


public class Editor extends JFrame {

    public JTextPane text_windows = new JTextPane(); //Text windows, edit window
    public JFileChooser File_selector = new JFileChooser(); //File selector
    public JTextField text = new JTextField(10);
    public Container container = getContentPane();

    public Editor() {
        super("My text editor");

        Action[] actions =            //Various functions of menu items
                {
                        new New_Act(),//[0]
                        new Open_Act(),//[1]
                        new Save_Act(),//[2]
                        new Cut_Act(),//[3]
                        new Copy_Act(),//[4]
                        new Paste_Act(),//[5]
                        new About_Act(),//[6]
                        new Exit_Act(),//[7]
                        new Help_Act(),//[8]
                        new Search_Act(),//[9]
                        new Time_and_date_Act(),//[10]
                        new Print(),
                        new Export(),
                };
        setJMenuBar(createJMenuBar(actions));        //Create menu bar based on actions
        container.add(text_windows, BorderLayout.CENTER);

        setSize(600, 650);
        setVisible(true);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    }

    private JMenuBar createJMenuBar(Action[] actions)    //Function to create menu bar
    {
        JMenuBar menu_bar = new JMenuBar();
        JMenu menuFile = new JMenu("File(F)");
        JMenu menuEdit = new JMenu("Edit(E)");
        JMenu menuAbout = new JMenu("Help(H)");
        JMenu menuSearch = new JMenu("Search(S)");
        JMenu menuTime_and_date = new JMenu("Time_and_date(T)");
        JMenu manageMenu = new JMenu("manageMenu");
        menuFile.add(new JMenuItem(actions[0]));
        menuFile.add(new JMenuItem(actions[1]));
        menuFile.add(new JMenuItem(actions[2]));
        menuFile.add(new JMenuItem(actions[7]));
        menuEdit.add(new JMenuItem(actions[3]));
        menuEdit.add(new JMenuItem(actions[4]));
        menuEdit.add(new JMenuItem(actions[5]));
        menuAbout.add(new JMenuItem(actions[6]));
        menuAbout.add(new JMenuItem(actions[8]));
        menuSearch.add(new JMenuItem(actions[9]));
        menuSearch.add(new JMenuItem(new NextSearch()));
        menuTime_and_date.add(new JMenuItem(actions[10]));
        manageMenu.add(new JMenuItem(actions[11]));
        manageMenu.add(new JMenuItem(actions[12]));
        menu_bar.add(menuFile);
        menu_bar.add(menuEdit);
        menu_bar.add(menuAbout);
        menu_bar.add(menuSearch);
        menu_bar.add(menuTime_and_date);
        menu_bar.add(manageMenu);
        return menu_bar;
    }


    class Print extends AbstractAction     //Time and Date
    {
        public Print() {
            super("Print");
        }

        public void actionPerformed(ActionEvent e) {
            int i = File_selector.showSaveDialog(Editor.this);
            if (i == JFileChooser.APPROVE_OPTION) {
                File f = File_selector.getSelectedFile();
                try {
                    FileOutputStream out = new FileOutputStream(f);
                    out.write(text_windows.getText().getBytes());
                    out.close();
                    HashPrintRequestAttributeSet hashPrintRequestAttributeSet = new HashPrintRequestAttributeSet();
                    DocFlavor flavor = DocFlavor.INPUT_STREAM.AUTOSENSE;
                    PrintService[] printService = PrintServiceLookup.lookupPrintServices(flavor, hashPrintRequestAttributeSet);
                    PrintService defaultService = PrintServiceLookup.lookupDefaultPrintService();
                    PrintService service = ServiceUI.printDialog(null, 200, 200, printService,
                            defaultService, flavor, hashPrintRequestAttributeSet);
                    if (service != null) {
                        try {
                            DocPrintJob job = service.createPrintJob();
                            Doc doc = new SimpleDoc(new FileInputStream(f), flavor, new HashDocAttributeSet());
                            job.print(doc, hashPrintRequestAttributeSet);
                        } catch (Exception event) {
                            event.printStackTrace();
                        }
                    }
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        }

        public void PDFprint(File file) throws Exception {
            PDDocument document = null;
            try {
                document = PDDocument.load(file);
                PrinterJob printJob = PrinterJob.getPrinterJob();
                printJob.setJobName(file.getName());
                // Find and set up a printer
                //Get all printers connected to this computer
                PrintService[] printServices = PrinterJob.lookupPrintServices();
                if (printServices == null || printServices.length == 0) {
                    System.out.print("Printing failed. No available printer was found. Please check.");
                    return;
                }
                PrintService printService = printServices[0];
                if (printService != null) {
                    printJob.setPrintService(printService);
                } else return;

                //Set paper and zoom
                PDFPrintable pdfPrintable = new PDFPrintable(document, Scaling.ACTUAL_SIZE);
                //Set up multi page printing
                Book book = new Book();
                PageFormat pageFormat = new PageFormat();
                //Set print direction
                pageFormat.setOrientation(PageFormat.PORTRAIT);
                pageFormat.setPaper(getPaper());
                book.append(pdfPrintable, pageFormat, document.getNumberOfPages());
                printJob.setPageable(book);
                printJob.setCopies(1);//Set the number of copies to be printed
                //Add print properties
                HashPrintRequestAttributeSet pars = new HashPrintRequestAttributeSet();
                pars.add(Sides.DUPLEX); //Setting single and double page
                printJob.print(pars);
            } finally {
                if (document != null) {
                    try {
                        document.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        public Paper getPaper() {
            Paper paper = new Paper();
            // The default is A4 paper, and the corresponding pixel width and height are 600 and 800 respectively
            int width = 600;
            int height = 800;
            // Set the margin in pixels, 20 mm margin, corresponding to 28px
            int marginLeft = 20;
            int marginRight = 0;
            int marginTop = 20;
            int marginBottom = 0;
            paper.setSize(width, height);
            paper.setImageableArea(marginLeft, marginRight, width - (marginLeft + marginRight), height - (marginTop + marginBottom));
            return paper; //solve the problem of blank print
        }
    }


    class Export extends AbstractAction     //Save as pdf
    {
        public Export() {
            super("Export as PDF");
        }

        public void actionPerformed(ActionEvent e) {
            int i = File_selector.showSaveDialog(Editor.this);
            if (i == JFileChooser.APPROVE_OPTION) {
                File f = File_selector.getSelectedFile();
                try {
                    this.exportAsPdf(f.getName());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }

        protected void exportAsPdf(String fileName) {
            try {
                PDDocument doc = new PDDocument();
                PDPage page = new PDPage();
                doc.addPage(page);
                PDPageContentStream content = new PDPageContentStream(doc, page);
                content.beginText();
                content.setFont(PDType1Font.HELVETICA, 16);
                content.showText(text_windows.getText());
                content.endText();
                content.close();
                doc.save(fileName);
                doc.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }


    class Time_and_date_Act extends AbstractAction     //Time and date
    {
        public Time_and_date_Act() {
            super("Time_and_date(T)     Ctrl+T");
        }

        public void actionPerformed(ActionEvent e) {
            // Instantiate a Date object
            Date date = new Date();

            // display time and date using toString()
            Frame f = new Frame();
            TextArea txt = new TextArea();
            txt.setText(new Date().toString());
            f.add(txt);
            f.pack();
            f.setVisible(true);


        }
    }


    class NextSearch extends AbstractAction {    //After pressing “Search”, enter the text, press “start finding”, and then click whether there is a second search

        public NextSearch() {
            super("start finding and finding next");
            text_windows.setSelectionColor(Color.BLUE);
        }

        @Override
        public void actionPerformed(ActionEvent e) {

            String word = text.getText().trim();
            if (!word.equals("")) {
                int numx = text_windows.getCaretPosition();
                int i = text_windows.getText().indexOf(word, numx);
                if (i >= 0) {
                    text_windows.setSelectionStart(i);
                    text_windows.setSelectionEnd(i + word.length());
                    text_windows.requestFocus();
                } else {
                    int num = 0;
                    int j = text_windows.getText().indexOf(word, num);
                    if (j < 0) {
                        JOptionPane.showMessageDialog((Component) null, "Not found:" + word, "Found:", 2);
                        return;
                    }
                    text_windows.setSelectionStart(j);
                    text_windows.setSelectionEnd(j + word.length());
                    text_windows.requestFocus();
                }
            }
            System.out.println(text_windows.getSelectedText());//Show the found part in idea
        }
    }


    class Search_Act extends AbstractAction       //Search
    {
        public Search_Act() {
            super("Search(S):(After pressing search, enter the search content at the bottom)      Alt+S");
        }

        public void actionPerformed(ActionEvent e) {
            container.add(text, BorderLayout.SOUTH);
        }
    }

    class New_Act extends AbstractAction        //New
    {
        public New_Act() {
            super("New(N)     Ctrl+N");
        }

        public void actionPerformed(ActionEvent e) {
            text_windows.setDocument(new DefaultStyledDocument());
        }
    }

    class Open_Act extends AbstractAction        //Open
    {
        public Open_Act() {
            super("Open(O)     Ctrl+O");
        }

        public void actionPerformed(ActionEvent e) {
            int i = File_selector.showOpenDialog(Editor.this);            //open file dialog box
            if (i == JFileChooser.APPROVE_OPTION)            //Click the dialog box to open the options
            {
                File f = File_selector.getSelectedFile();    //get file
                try {
                    InputStream is = new FileInputStream(f);
                    text_windows.read(is, "d");
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }

    class Save_Act extends AbstractAction        //Save
    {
        public Save_Act() {
            super("Save(S)     Ctrl+S");
        }

        public void actionPerformed(ActionEvent e) {
            int i = File_selector.showSaveDialog(Editor.this);
            if (i == JFileChooser.APPROVE_OPTION) {
                File f = File_selector.getSelectedFile();
                try {
                    FileOutputStream out = new FileOutputStream(f);
                    out.write(text_windows.getText().getBytes());
                } catch (Exception ex) {
                    ex.printStackTrace();
                }
            }
        }
    }


    class Exit_Act extends AbstractAction        //EXIT
    {
        public Exit_Act() {
            super("Exit(X)     Ctrl+Q");
        }

        public void actionPerformed(ActionEvent e) {
            dispose();
            int i = JOptionPane.showConfirmDialog(null, "Has it been saved?", "Exit",
                    JOptionPane.YES_NO_OPTION);
        }
    }

    class Cut_Act extends AbstractAction        //CUT
    {
        public Cut_Act() {
            super("Shear_Cut(T)     Ctrl+X");
        }

        public void actionPerformed(ActionEvent e) {
            text_windows.cut();
        }
    }

    class Copy_Act extends AbstractAction        //COPY
    {
        public Copy_Act() {
            super("Copy(C)     Ctrl+C");
        }

        public void actionPerformed(ActionEvent e) {
            text_windows.copy();
        }
    }

    class Paste_Act extends AbstractAction        //PASTE
    {
        public Paste_Act() {
            super("Paste(P)     Ctrl+V");
        }

        public void actionPerformed(ActionEvent e) {
            text_windows.paste();
        }
    }

    class About_Act extends AbstractAction {
        public About_Act() {
            super("About My Text Editor(A)");
        }

        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(Editor.this, "authors:1-Charles Zheng 2-Zheng Chun", "About", JOptionPane.PLAIN_MESSAGE);
        }
    }

    class Help_Act extends AbstractAction {
        public Help_Act() {
            super("Contact author");
        }

        public void actionPerformed(ActionEvent e) {
            JOptionPane.showMessageDialog(Editor.this, "471993057@qq.com", "Author's Email", JOptionPane.PLAIN_MESSAGE);
        }

    }

    public static void main(String[] args) {
        new Editor();
    }
}

