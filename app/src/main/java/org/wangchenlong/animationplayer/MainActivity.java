package org.wangchenlong.animationplayer;

import android.animation.IntEvaluator;
import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.support.annotation.DrawableRes;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainActivity extends AppCompatActivity {
    @BindView(R.id.main_rv_grid) RecyclerView mRvGrid;

    private Context mContext;
    private ArrayList<Animation> mAnimations;
    private @DrawableRes int[] mImages = {
            R.drawable.jessica_square,
            R.drawable.tiffany_square,
            R.drawable.taeyeon_square,
            R.drawable.yoona_square,
            R.drawable.yuri_square,
            R.drawable.soo_square,
            R.drawable.seo_square,
            R.drawable.kim_square,
            R.drawable.sunny_square
    };
    private @DrawableRes int mFrame = R.drawable.anim_images;
    private String[] mTexts = {
            "平移",
            "缩放",
            "旋转",
            "透明",
            "混合",
            "自定",
            "帧动",
            "属性",
            "差值"
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ButterKnife.bind(this);

        mContext = getApplicationContext();

        initAnimations(mContext);

        mRvGrid.setLayoutManager(new GridLayoutManager(mContext, 2)); // 每行两列
        mRvGrid.setAdapter(new GridAdapter(mContext, mAnimations, mFrame, mTexts, mImages));
    }

    private void initAnimations(Context context) {
        mAnimations = new ArrayList<>();
        mAnimations.add(AnimationUtils.loadAnimation(context, R.anim.anim_translate));
        mAnimations.add(AnimationUtils.loadAnimation(context, R.anim.anim_scale));
        mAnimations.add(AnimationUtils.loadAnimation(context, R.anim.anim_rotate));
        mAnimations.add(AnimationUtils.loadAnimation(context, R.anim.anim_alpha));
        mAnimations.add(AnimationUtils.loadAnimation(context, R.anim.anim_all));

        final Rotate3dAnimation anim = new Rotate3dAnimation(0.0f, 720.0f, 100.0f, 100.0f, 0.0f, false);
        anim.setDuration(2000);
        mAnimations.add(anim);
    }

    private static class GridAdapter extends RecyclerView.Adapter<GridViewHolder> {

        private ArrayList<Animation> mAnimations;
        private @DrawableRes int mFrame;
        private String[] mTexts;
        private @DrawableRes int[] mImages;

        private int mLastPosition = -1;
        private Context mContext;

        public GridAdapter(Context context,
                           ArrayList<Animation> animations, @DrawableRes int frame,
                           String[] texts, @DrawableRes int[] images) {
            mContext = context;
            mAnimations = animations;
            mFrame = frame;
            mTexts = texts;
            mImages = images;
        }

        @Override public GridViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_anim, parent, false);
            return new GridViewHolder(view);
        }

        @Override public void onBindViewHolder(final GridViewHolder holder, final int position) {
            holder.getImageView().setImageResource(mImages[position]);
            holder.getButton().setText(mTexts[position]);

            switch (position) {
                case 8:
                    performValueAnimation(holder.getImageView(), 0, Utils.dp2px(mContext, 120));
                    holder.getButton().setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            performValueAnimation(holder.getImageView(), 0, Utils.dp2px(mContext, 120));
                        }
                    });
                    break;
                case 7:
                    performValueAnimation(holder.getImageView());
                    holder.getButton().setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            performValueAnimation(holder.getImageView());
                        }
                    });
                    break;
                case 6: // 使用帧动画
                    holder.getImageView().setImageResource(mFrame);
                    holder.getButton().setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            ((AnimationDrawable) holder.getImageView().getDrawable()).start();
                        }
                    });
                    break;
                default:
                    holder.getImageView().setAnimation(mAnimations.get(position));
                    holder.getButton().setOnClickListener(new View.OnClickListener() {
                        @Override public void onClick(View v) {
                            holder.getImageView().startAnimation(mAnimations.get(position));
                        }
                    });
                    break;
            }

            setAnimation(holder.getContainer(), position);
        }

        private void setAnimation(View viewToAnimate, int position) {
            if (position > mLastPosition || mLastPosition == -1) {
                Animation animation = AnimationUtils.loadAnimation(mContext, android.R.anim.slide_in_left);
                viewToAnimate.startAnimation(animation);
                mLastPosition = position;
            }
        }

        private void performValueAnimation(View view) {
            ViewWrapper vw = new ViewWrapper(view);
            ObjectAnimator.ofInt(vw, "width", 0, Utils.dp2px(view.getContext(), 120))
                    .setDuration(2000).start(); // 启动动画
        }

        private void performValueAnimation(final View target, final int start, final int end) {
            ValueAnimator valueAnimator = ValueAnimator.ofInt(1, 100);
            valueAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {

                // 持有一个IntEvaluator对象，方便下面估值的时候使用
                private IntEvaluator mEvaluator = new IntEvaluator();

                @Override
                public void onAnimationUpdate(ValueAnimator animator) {
                    // 获得当前动画的进度值，整型，1-100之间
                    int currentValue = (Integer) animator.getAnimatedValue();

                    // 获得当前进度占整个动画过程的比例，浮点型，0-1之间
                    float fraction = animator.getAnimatedFraction();

                    // 直接调用整型估值器通过比例计算出宽度，然后再设给Button
                    target.getLayoutParams().width = mEvaluator.evaluate(fraction, start, end);
                    target.requestLayout();
                }
            });

            valueAnimator.setDuration(2000).start();
        }


        private static class ViewWrapper {
            private View mTarget;

            public ViewWrapper(View target) {
                mTarget = target;
            }

            public int getWidth() {
                return mTarget.getLayoutParams().width;
            }

            public void setWidth(int width) {
                mTarget.getLayoutParams().width = width;
                mTarget.requestLayout();
            }
        }

        @Override public int getItemCount() {
            return mTexts.length;
        }
    }

    private static class GridViewHolder extends RecyclerView.ViewHolder {
        private ImageView mImageView;
        private Button mButton;
        private View mContainer;

        public GridViewHolder(View itemView) {
            super(itemView);
            mContainer = itemView;
            mButton = (Button) itemView.findViewById(R.id.item_b_start);
            mImageView = (ImageView) itemView.findViewById(R.id.item_iv_img);
        }

        public View getContainer() {
            return mContainer;
        }

        public ImageView getImageView() {
            return mImageView;
        }

        public Button getButton() {
            return mButton;
        }
    }
}
