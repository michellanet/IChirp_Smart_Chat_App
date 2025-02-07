package com.mbwasi.ichirp.Adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.mbwasi.ichirp.MessageActivity;
import com.mbwasi.ichirp.Model.Chat;
import com.mbwasi.ichirp.Model.User;
import com.mbwasi.ichirp.R;

import org.apache.commons.text.WordUtils;

import java.util.List;

public class UserAdapter extends RecyclerView.Adapter<UserAdapter.ViewHoler> {

    private Context mContext;
    private List<User> mUsers;
    private boolean isChat;
    private String lastMessage;

    public UserAdapter(Context mContext, List<User> mUsers, boolean isChat) {
        this.mContext = mContext;
        this.mUsers = mUsers;
        this.isChat = isChat;
    }

    @NonNull
    @Override
    public ViewHoler onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.user_item, parent, false);
        return new UserAdapter.ViewHoler(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHoler holder, int position) {
        final User user = mUsers.get(position);
        holder.username.setText(WordUtils.capitalizeFully(user.getUsername()));
        if (user.getImageURL().equals("default")) {
            holder.profile_image.setImageResource(R.mipmap.ic_launcher);
        } else {
            Glide.with(mContext).load(user.getImageURL()).into(holder.profile_image);
        }

        if (isChat) {
            lastMessage(user.getId(), holder.lastMsg);

            if (user.getStatus().equals("online")) {
                holder.imgOn.setVisibility(View.VISIBLE);
                holder.imgOff.setVisibility(View.GONE);
            } else {
                holder.imgOff.setVisibility(View.VISIBLE);
                holder.imgOn.setVisibility(View.GONE);
            }
        } else {
            holder.lastMsg.setVisibility(View.GONE);
            holder.imgOff.setVisibility(View.GONE);
            holder.imgOn.setVisibility(View.GONE);
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(mContext, MessageActivity.class);
                intent.putExtra("userId", user.getId());
                mContext.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public class ViewHoler extends RecyclerView.ViewHolder {
        private TextView username, lastMsg;
        private ImageView profile_image, imgOn, imgOff;

        public ViewHoler(@NonNull View itemView) {
            super(itemView);

            username = itemView.findViewById(R.id.username);
            profile_image = itemView.findViewById(R.id.profile_image);
            imgOn = itemView.findViewById(R.id.img_on);
            imgOff = itemView.findViewById(R.id.img_off);
            lastMsg = itemView.findViewById(R.id.lastMsg);
        }
    }

    private void lastMessage(final String userId, final TextView lastMsg) {
        lastMessage = "default";
        final FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Chats");

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snap : snapshot.getChildren()) {
                    Chat chat = snap.getValue(Chat.class);
                    if (firebaseUser != null && firebaseUser.getUid() != null && chat.getReceiver().equals(firebaseUser.getUid()) && chat.getSender().equals(userId)
                            || firebaseUser != null && chat.getReceiver().equals(userId) && chat.getSender().equals(firebaseUser.getUid())) {
                        lastMessage = chat.getMessage();
                    }
                }

                switch (lastMessage) {
                    case "default":
                        lastMsg.setText("No message");
                        break;
                    default:
                        lastMsg.setText(lastMessage);
                        break;
                }
                lastMessage = "default";
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
}
