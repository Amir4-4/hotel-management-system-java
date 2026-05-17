import java.util.ArrayList;

public class GuestBST {
    private GuestNode root;

    private class GuestNode {
        Guest guest;
        GuestNode left, right;

        GuestNode(Guest guest) { this.guest = guest; }
    }

    public void add(Guest g) { root = insert(root, g); }

    private GuestNode insert(GuestNode node, Guest g) {
        if (node == null) return new GuestNode(g);
        if (g.getGuestID() < node.guest.getGuestID()) node.left = insert(node.left, g);
        else node.right = insert(node.right, g);
        return node;
    }

    public ArrayList<Guest> toArrayList() {
        ArrayList<Guest> list = new ArrayList<>();
        inOrder(root, list);
        return list;
    }

    private void inOrder(GuestNode node, ArrayList<Guest> list) {
        if (node == null) return;
        inOrder(node.left, list);
        list.add(node.guest);
        inOrder(node.right, list);
    }
}
