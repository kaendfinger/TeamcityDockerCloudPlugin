package run.var.teamcity.cloud.docker.util;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.JsonNode;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.io.InputStream;

/**
 * Immutable JSON node. Nodes instanced from this class are safe to use from multiple threads.
 */
public class Node extends AbstractNode<Node> {

    /**
     * Constant node for an empty array.
     */
    public static Node EMPTY_ARRAY = new Node(OBJECT_MAPPER.createArrayNode());

    /**
     * Constant node for an empty object.
     */
    public static Node EMPTY_OBJECT = new Node(OBJECT_MAPPER.createObjectNode());

    Node(JsonNode node) {
        super(node);
    }

    @Override
    Node newNode(JsonNode node) {
        return new Node(node);
    }

    public static Node parse(InputStream jsonStream) throws IOException {
        return new Node(OBJECT_MAPPER.readTree(jsonStream));
    }

    /**
     * Parse an input stream of node as a stream of nodes.
     * <p>
     * The implementation must be able to handle at least whitespace characters between the node themselves.
     * </p>
     *
     * @param jsonStream the input stream
     *
     * @return the stream of nodes
     *
     * @throws IOException if an error occurred while processing the stream
     */
    @Nonnull
    public static NodeStream parseMany(final InputStream jsonStream) throws IOException {
        final JsonParser parser = JSON_FACTORY.createParser(jsonStream);
        return new NodeStream() {
            @Override
            public Node next() throws IOException {
                if (parser.nextToken() == null) {
                    return null;
                }
                return new Node(parser.readValueAs(JsonNode.class));
            }

            @Override
            public void close() throws IOException {
                jsonStream.close();
            }
        };
    }

    public static Node parse(String json) throws IOException {
        return new Node(OBJECT_MAPPER.readTree(json));
    }

    /**
     * Creates an editable node using this location as root. Changes made on the returned instance will not affect this
     * node. The returned instance can be made immutable again by calling {@link EditableNode#saveNode()}.
     *
     * @return an editable using this node location as content root
     */
    public EditableNode editNode() {
        return new EditableNode(node.deepCopy());
    }
}
