package dev.tarclient.launcher;
import com.google.gson.*;
import dev.tarclient.config.*;
import javax.swing.*;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.*;
import java.util.function.*;
/** Explicitly opened social page. Text messages are never interpreted as HTML. */
public final class SocialPanel extends JPanel {
    private final Supplier<MicrosoftAuth.Session> session;
    private final SocialClient api=new SocialClient();
    private final ExecutorService worker=Executors.newSingleThreadExecutor(r->Thread.ofVirtual().name("Tar social UI").unstarted(r));
    private final javax.swing.Timer timer;
    private final DefaultListModel<SocialState.Person> people=new DefaultListModel<>();
    private final JList<SocialState.Person> friends=new JList<>(people){
        protected void paintComponent(Graphics g){super.paintComponent(g);if(people.isEmpty()){g.setColor(LauncherTheme.MUTED);g.drawString("No friends or requests yet",16,32);}}
    };
    private final JTextArea history=new JTextArea();
    private final JTextField message=new JTextField(),name=new JTextField();
    private final JLabel status=new JLabel("Connect with friends and personalize your player. Everything is free.");
    private final JPanel outfits=new JPanel(new GridLayout(0,2,10,10));
    private SocialState state=SocialState.EMPTY;
    private String identity="",selected="";
    private boolean busy,closed;
    public SocialPanel(Supplier<MicrosoftAuth.Session> session){
        super(new BorderLayout(10,14));this.session=session;
        var tabs=new JTabbedPane();tabs.putClientProperty("FlatLaf.style","underlineColor: #"+String.format("%06X",LauncherTheme.ACCENT.getRGB()&0xFFFFFF));tabs.addTab("Friends & messages",friendsPanel());
        var cosmetics=new JPanel(new BorderLayout(0,12));cosmetics.add(new JLabel("All cosmetics are free. Click to equip / remove; one per slot."),BorderLayout.NORTH);cosmetics.add(new JScrollPane(outfits));tabs.addTab("Cosmetics",cosmetics);
        var emotes=new JPanel(new BorderLayout(0,12));emotes.add(new JLabel("Play emotes in Minecraft: Right Shift > Friends & style > Emotes. All free."),BorderLayout.NORTH);var catalog=new JPanel(new GridLayout(0,2,10,10));for(var item:SocialCatalog.EMOTES){var label=new JLabel(item.name()+" · Free");label.setBorder(BorderFactory.createEmptyBorder(16,16,16,16));catalog.add(label);}emotes.add(new JScrollPane(catalog));tabs.addTab("Emotes",emotes);
        add(tabs);add(status,BorderLayout.SOUTH);wardrobe();timer=new javax.swing.Timer(6000,e->refresh());
        addHierarchyListener(e->{if(isShowing()){if(!timer.isRunning()){timer.start();refresh();}}else timer.stop();});
    }
    private JPanel friendsPanel(){
        var panel=new JPanel(new BorderLayout(12,12));var request=new JPanel(new BorderLayout(8,0));name.putClientProperty("JTextField.placeholderText","Minecraft Java username");request.add(name);request.add(button("Send friend request",()->call("request",SocialClient.body("name",name.getText().trim()),r->{name.setText("");status.setText("Friend request sent");refresh();})),BorderLayout.EAST);panel.add(request,BorderLayout.NORTH);
        friends.setBackground(LauncherTheme.CARD);friends.setForeground(LauncherTheme.TEXT);friends.setSelectionBackground(LauncherTheme.ACCENT);friends.setSelectionForeground(LauncherTheme.accentText());
        friends.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);friends.setFixedCellHeight(44);friends.addListSelectionListener(e->{if(!e.getValueIsAdjusting()){var p=friends.getSelectedValue();String id=p==null?"":p.uuid();if(!id.equals(selected)){selected=id;history.setText("");message.setText("");}messages();}});
        var left=new JPanel(new BorderLayout(0,10));left.add(new JScrollPane(friends));var actions=new JPanel(new GridLayout(2,2,6,6));for(String action:List.of("accept","remove","block","unblock"))actions.add(button(action.substring(0,1).toUpperCase()+action.substring(1),()->respond(action)));left.add(actions,BorderLayout.SOUTH);
        var chat=new JPanel(new BorderLayout(0,10));history.setEditable(false);history.setLineWrap(true);history.setWrapStyleWord(true);history.setMargin(new Insets(14,14,14,14));chat.add(new JScrollPane(history));var compose=new JPanel(new BorderLayout(6,0));message.putClientProperty("JTextField.placeholderText","Message an accepted friend (up to 1,000 characters)");compose.add(message);compose.add(button("Send",this::send),BorderLayout.EAST);message.addActionListener(e->send());chat.add(compose,BorderLayout.SOUTH);
        var split=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,left,chat);split.setResizeWeight(.34);split.setDividerLocation(280);panel.add(split);return panel;
    }
    private JButton button(String label,Runnable action){var b=new JButton(label);b.addActionListener(e->action.run());return b;}
    private void call(String action,JsonObject body,Consumer<JsonObject> then){
        if(busy||closed)return;var auth=session.get();if(auth==null||auth.demo()){status.setText("Sign in to your Minecraft Java account from Accounts first.");return;}
        busy=true;worker.submit(()->{try{var result=api.request(auth,action,body);SwingUtilities.invokeLater(()->{busy=false;if(closed)return;var current=session.get();if(current==null||!current.uuid().equals(auth.uuid())){reset();return;}then.accept(result);});}catch(Exception error){SwingUtilities.invokeLater(()->{busy=false;if(!closed)status.setText(error.getMessage()==null?"Community service unavailable":error.getMessage());});}});
    }
    private void reset(){identity="";state=SocialState.EMPTY;people.clear();selected="";history.setText("");message.setText("");wardrobe();}
    private void refresh(){
        var auth=session.get();String id=auth==null?"":auth.uuid();if(!identity.equals(id)){reset();identity=id;}
        call("state",new JsonObject(),r->{var fresh=SocialState.parse(r);String keep=selected;state=fresh;
            if(!Collections.list(people.elements()).equals(state.people())){people.clear();for(var p:state.people())people.addElement(p);for(int i=0;i<people.size();i++)if(people.get(i).uuid().equals(keep))friends.setSelectedIndex(i);}
            wardrobe();status.setText("All items free · "+(PrivacyPreferences.defaults().sharingEnabled()?"Player sharing enabled":"Player sharing is off in Settings"));messages();});
    }
    private void wardrobe(){outfits.removeAll();for(var item:SocialCatalog.COSMETICS){boolean on=state.cosmetics().contains(item.id());var b=button((on?"✓ ":"")+item.name()+" · Free",()->call("equip",SocialState.equipment(SocialCatalog.equip(state.cosmetics(),item.id())),r->refresh()));b.setPreferredSize(new Dimension(230,48));if(on)b.setForeground(LauncherTheme.ACCENT);outfits.add(b);}outfits.revalidate();outfits.repaint();}
    private void respond(String action){var p=friends.getSelectedValue();if(p==null){status.setText("Select a player first.");return;}if((action.equals("remove")||action.equals("block"))&&JOptionPane.showConfirmDialog(this,"This deletes your shared message history. Continue?",action+" "+p.name(),JOptionPane.YES_NO_OPTION)!=JOptionPane.YES_OPTION)return;call("respond",SocialClient.body("target",p.uuid(),"action",action),r->refresh());}
    private void messages(){var p=friends.getSelectedValue();if(p==null||!p.kind().equals("friend"))return;String id=p.uuid();call("messages",SocialClient.body("target",id),r->{if(selected.equals(id))display(r,p.name());});}
    private void display(JsonObject response,String friend){var text=new StringBuilder();var auth=session.get();String me=auth==null?"":auth.uuid().replace("-","");for(var m:SocialState.messages(response)){text.append(m.sender().equals(me)?"You":friend).append(": ").append(m.text()).append("\n\n");}if(!history.getText().equals(text.toString())){history.setText(text.toString());history.setCaretPosition(history.getDocument().getLength());}}
    private void send(){var p=friends.getSelectedValue();String draft=message.getText();if(p==null||!p.kind().equals("friend")){status.setText("Select an accepted friend first.");return;}if(draft.isBlank()||draft.length()>1000){status.setText("Messages must contain 1–1,000 characters.");return;}call("send",SocialClient.body("target",p.uuid(),"text",draft,"nonce",UUID.randomUUID().toString()),r->{if(selected.equals(p.uuid())){display(r,p.name());if(message.getText().equals(draft))message.setText("");}});}
    public void dispose(){closed=true;timer.stop();worker.shutdownNow();}
}
