public class ExtraGroupAdapter extends SimpleCursorTreeAdapter {

    private Cursor mExtraChildCursor;       

    @Override
    protected Cursor getChildrenCursor(Cursor groupCursor) {
        // if groupCursor.getPosition() returns -1 we have to provide the Cursor for our fake group
    }

    @Override
    public int getGroupCount() {
        // 1 more
        return super.getGroupCount() + 1;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded,
            View convertView, ViewGroup parent) {
        if (groupPosition == 0) {
            if (getCursor() == null) {
                throw new IllegalStateException(
                        "this should only be called when the cursor is valid");
            }
            View v;
            if (convertView == null) {
                v = newGroupView(mContext, getCursor(), isExpanded, parent);
            } else {
                v = convertView;
            }
            // if it's position 0 move the group Cursor to position -1 to
            // let the bindGroupView method that is deals with our fake row
            getCursor().moveToPosition(-1);
            bindGroupView(v, mContext, getCursor(), isExpanded);
            return v;
        } else {
            return super.getGroupView(groupPosition - 1, isExpanded,
                    convertView, parent);
        }
    }

    @Override
    protected void bindGroupView(View view, Context context, Cursor cursor,
            boolean isExpanded) {
        if (cursor.getPosition() == -1) {
            // bind our fake row, unfortunately this must be done manually
        } else {
            super.bindGroupView(view, context, cursor, isExpanded);
        }
    }

    @Override
    public View getChildView(int groupPosition, int childPosition,
            boolean isLastChild, View convertView, ViewGroup parent) {
        if (groupPosition == 0) {
            // replicate what the CursorTreeAdapter does for our fake group
            // position 0
            if (getCursor() == null) {
                throw new IllegalStateException(
                        "this should only be called when the cursor is valid");
            }
            View v;
            getCursor().moveToPosition(-1);
            mExtraChildCursor.moveToPosition(childPosition);
            if (convertView == null) {
                v = newChildView(mContext, mExtraChildCursor, isLastChild,
                        parent);
            } else {
                v = convertView;
            }
            bindChildView(v, mContext, mExtraChildCursor, isLastChild);
            return v;
        } else {
            // use the default implementation but offset the Cursor's
            // current position
            return super.getChildView(groupPosition - 1, childPosition,
                    isLastChild, convertView, parent);
        }
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        if (groupPosition == 0) {
            // implement the trick
            if (mExtraChildCursor == null) {
                getCursor().moveToPosition(-1); // in the getChildrenCursor
                                                // a Cursor position of -1
                                                // means it is the fake row
                mExtraChildCursor = getChildrenCursor(getCursor());
                return 0;
            } else {
                return mExtraChildCursor.getCount();
            }
        }
        return super.getChildrenCount(groupPosition);
    }

    @Override
    public void setChildrenCursor(int groupPosition, Cursor childrenCursor) {
        if (groupPosition == 0) {
            // hold a reference to the extra Cursor
            mExtraChildCursor = childrenCursor;
            notifyDataSetChanged();
        } else {
            super.setChildrenCursor(groupPosition, childrenCursor);
        }
    }

}