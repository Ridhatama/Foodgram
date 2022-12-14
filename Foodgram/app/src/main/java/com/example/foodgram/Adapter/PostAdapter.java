package com.example.foodgram.Adapter;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.foodgram.CommentsActivity;
import com.example.foodgram.Fragment.BottomNav;
import com.example.foodgram.Fragment.ProfileFragment;
import com.example.foodgram.Model.Post;
import com.example.foodgram.Model.User;
import com.example.foodgram.PostDetailActivity;
import com.example.foodgram.R;
import com.google.android.material.bottomsheet.BottomSheetDialog;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.ViewHolder> {

    public Context mContext;
    public List<Post> mPost;


    private FirebaseUser firebaseUser;

    public PostAdapter(Context mContext, List<Post> mPost) {
        this.mContext = mContext;
        this.mPost = mPost;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.post_item, viewGroup, false);
        return new PostAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        firebaseUser = FirebaseAuth.getInstance().getCurrentUser();
        Post post = mPost.get(position);

        Glide.with(mContext).load(post.getPostimage()).into(holder.post_image);


        //Show Desc POST
        if (post.getJudul().equals("")) {
            holder.judulpost.setVisibility(View.GONE);
        } else {
            holder.judulpost.setVisibility(View.VISIBLE);
            holder.judulpost.setText(post.getJudul());
        }

        if (post.getDescription().equals("")) {
            holder.description.setVisibility(View.GONE);
        } else {
            holder.description.setVisibility(View.VISIBLE);
            holder.description.setText(post.getDescription().replace("\\n", "\n"));
        }

        publisherInfo(holder.image_profile, holder.usernamepost, holder.publisher, post.getPublisher());
        isLikes(post.getPostid(), holder.like);
        isBookmark(post.getPostid(), holder.bookmarkpost);
        nrLikes(holder.likes, post.getPostid());
        getComments(post.getPostid(), holder.comments);

        holder.image_profile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mContext instanceof BottomNav) {
                    SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                    editor.putString("profileid", post.getPublisher());
                    editor.apply();

                    ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.container, new ProfileFragment()).commit();
                } else {
                    Intent intent = new Intent(mContext, BottomNav.class);
                    intent.putExtra("publisherid", post.getPublisher());
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    mContext.startActivity(intent);
                }
            }
        });

        holder.usernamepost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.container, new ProfileFragment()).commit();
            }
        });

        holder.publisher.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("profileid", post.getPublisher());
                editor.apply();

                ((FragmentActivity) mContext).getSupportFragmentManager().beginTransaction().replace(R.id.container, new ProfileFragment()).commit();
            }
        });

        holder.post_image.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                SharedPreferences.Editor editor = mContext.getSharedPreferences("PREFS", Context.MODE_PRIVATE).edit();
                editor.putString("postid", post.getPostid());
                editor.apply();

                Intent intent = new Intent(mContext, PostDetailActivity.class);
                mContext.startActivity(intent);

            }
        });

        holder.bookmarkpost.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.bookmarkpost.getTag().equals("unbookmarked")) {
                    FirebaseDatabase.getInstance().getReference().child("Bookmark").child(firebaseUser.getUid())
                            .child(post.getPostid()).setValue(true);
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Bookmark").child(firebaseUser.getUid())
                            .child(post.getPostid()).removeValue();
                }
            }
        });

        holder.like.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (holder.like.getTag().equals("like")) {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid())
                            .child(firebaseUser.getUid()).setValue(true);
                    addNotifications(post.getPublisher(), post.getPostid());
                } else {
                    FirebaseDatabase.getInstance().getReference().child("Likes").child(post.getPostid())
                            .child(firebaseUser.getUid()).removeValue();
                }
            }
        });

        holder.comment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("publisherid", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        holder.comments.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(mContext, CommentsActivity.class);
                intent.putExtra("postid", post.getPostid());
                intent.putExtra("publisherid", post.getPublisher());
                mContext.startActivity(intent);
            }
        });

        holder.postsetting.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final BottomSheetDialog bottomSheetDialog = new BottomSheetDialog(
                        mContext, R.style.BottomSheetDialogTheme
                );
                View bottomSheetView = LayoutInflater.from(mContext)
                        .inflate(R.layout.setting_sheet_bottom, null);

                bottomSheetView.findViewById(R.id.unfollow).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(firebaseUser.getUid())
                                .child("following").child(post.getPublisher()).removeValue();
                        FirebaseDatabase.getInstance().getReference().child("Follow").child(post.getPublisher())
                                .child("followers").child(firebaseUser.getUid()).removeValue();
                        bottomSheetDialog.cancel();
                    }
                });

                bottomSheetView.findViewById(R.id.report).setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        final BottomSheetDialog bottomSheetDialog1 = new BottomSheetDialog(mContext, R.style.BottomSheetDialogTheme);
                        View bottomSheetReport = LayoutInflater.from(mContext).inflate(R.layout.report_sheet_bottom, null);
                        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Report").child(post.getPostid());
                        HashMap<String, Object> hashMap = new HashMap<>();
                        bottomSheetReport.findViewById(R.id.reportText_1).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                hashMap.put("report1", "Informasi Palsu");
                                reference.child(firebaseUser.getUid()).updateChildren(hashMap);
                                bottomSheetDialog1.cancel();
                            }
                        });

                        bottomSheetReport.findViewById(R.id.reportText_2).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                hashMap.put("report2", "Pelanggaran hak kekayaan intelektual");
                                reference.child(firebaseUser.getUid()).updateChildren(hashMap);
                                bottomSheetDialog1.cancel();
                            }
                        });

                        bottomSheetReport.findViewById(R.id.reportText_3).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                hashMap.put("report3", "Bukan Makanan");
                                reference.child(firebaseUser.getUid()).updateChildren(hashMap);
                                bottomSheetDialog1.cancel();
                            }
                        });

                        bottomSheetReport.findViewById(R.id.reportText_4).setOnClickListener(new View.OnClickListener() {
                            @Override
                            public void onClick(View view) {
                                hashMap.put("report4", "Ujaran kebencian");
                                reference.child(firebaseUser.getUid()).updateChildren(hashMap);
                                bottomSheetDialog1.cancel();
                            }
                        });
                        bottomSheetDialog.cancel();
                        bottomSheetDialog1.setContentView(bottomSheetReport);
                        bottomSheetDialog1.show();
                    }
                });

                bottomSheetDialog.setContentView(bottomSheetView);
                bottomSheetDialog.show();

            }
        });

    }

    @Override
    public int getItemCount() {
        return mPost.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public ImageView image_profile, post_image, like, comment, postsetting, bookmarkpost;
        public TextView usernamepost, likes, publisher, description, judulpost, comments;


        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            bookmarkpost = itemView.findViewById(R.id.bookmarkpost);
            image_profile = itemView.findViewById(R.id.image_profile);
            post_image = itemView.findViewById(R.id.post);
            like = itemView.findViewById(R.id.likepost);
            comment = itemView.findViewById(R.id.commentpost);
            usernamepost = itemView.findViewById(R.id.usernamepost);
            likes = itemView.findViewById(R.id.likecountpost);
            publisher = itemView.findViewById(R.id.publisherpost);
            description = itemView.findViewById(R.id.descriptionpost);
            judulpost = itemView.findViewById(R.id.judulpost);
            comments = itemView.findViewById(R.id.commentspost);
            postsetting = itemView.findViewById(R.id.postsetting);
        }
    }

    private void getComments(String postid, final TextView comments) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Comments").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                comments.setText("View all " + snapshot.getChildrenCount() + " Comments");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isBookmark(String postid, ImageView imageView) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Bookmark")
                .child(firebaseUser.getUid());

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.child(postid).exists()) {
                    imageView.setImageResource(R.drawable.bookmark_fill);
                    imageView.setTag("bookmarked");
                } else {
                    imageView.setImageResource(R.drawable.bookmark);
                    imageView.setTag("unbookmarked");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void isLikes(String postid, ImageView imageView) {
        FirebaseUser firebaseUser = FirebaseAuth.getInstance().getCurrentUser();

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference()
                .child("Likes")
                .child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.child(firebaseUser.getUid()).exists()) {
                    imageView.setImageResource(R.drawable.heartfill);
                    imageView.setTag("liked");
                } else {
                    imageView.setImageResource(R.drawable.heart);
                    imageView.setTag("like");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void addNotifications(String notifto, String postid) {

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Notifications").child(notifto);

        String notifid = reference.push().getKey();

        HashMap<String, Object> hashMap = new HashMap<>();

        hashMap.put("userid", firebaseUser.getUid());
        hashMap.put("text", "liked your post");
        hashMap.put("postid", postid);
        hashMap.put("ispost", true);
        hashMap.put("notifid", notifid);
        hashMap.put("key", firebaseUser.getUid() + postid);

        reference.orderByChild("key").equalTo(firebaseUser.getUid() + postid).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
//                    snapshot.getRef().removeValue();
                } else {
                    reference.child(notifid).setValue(hashMap);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void publisherInfo(ImageView image_profile, TextView usernamepost, TextView publisher, String userid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Users").child(userid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                User user = dataSnapshot.getValue(User.class);
                Glide.with(mContext).load(user.getImageurl()).into(image_profile);
                usernamepost.setText(user.getNama());
                publisher.setText(user.getNama());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

    }

    private void nrLikes(final TextView likes, String postid) {
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference().child("Likes").child(postid);

        reference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                likes.setText(snapshot.getChildrenCount() + " likes");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }


}
