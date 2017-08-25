/**
 * The MIT License (MIT)
 *
 * MSUSEL C# Parser
 * Copyright (c) 2015-2017 Montana State University, Gianforte School of Computing,
 * Software Engineering Laboratory
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */
package edu.montana.gsoc.msusel.parsers;

import java.util.List;
import java.util.Stack;

import org.antlr.v4.runtime.ParserRuleContext;
import org.eclipse.jdt.annotation.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sparqline.metrics.loc.LoCCounter;

import edu.montana.gsoc.msusel.INode;
import edu.montana.gsoc.msusel.node.FieldNode;
import edu.montana.gsoc.msusel.node.FileNode;
import edu.montana.gsoc.msusel.node.MethodNode;
import edu.montana.gsoc.msusel.node.StatementNode;
import edu.montana.gsoc.msusel.node.StatementType;
import edu.montana.gsoc.msusel.node.TypeNode;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6BaseListener;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Accessor_declarationsContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Add_accessor_declarationContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Break_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Checked_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Class_bodyContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Class_definitionContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Compilation_unitContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Constructor_bodyContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Constructor_declaration2Context;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Constructor_declarationContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Constructor_declaratorContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Continue_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Declaration_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Delegate_declarationContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Delegate_definitionContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Delegate_typeContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Destructor_bodyContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Destructor_declarationContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Do_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Embedded_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Embedded_statement_unsafeContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Empty_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Enum_bodyContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Enum_definitionContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Enum_member_declarationContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Event_declaration2Context;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Event_declarationContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Expression_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Field_declaration2Context;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Fixed_parameterContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Fixed_parametersContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Fixed_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.For_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Foreach_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Formal_parameter_listContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Goto_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.IdentifierContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.If_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Indexer_declaration2Context;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Indexer_declarationContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Indexer_declaratorContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Interface_bodyContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Interface_definitionContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Interface_event_declaration2Context;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Interface_event_declarationContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Interface_indexer_declaration2Context;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Interface_indexer_declarationContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Interface_method_declaration2Context;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Interface_method_declarationContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Interface_property_declaration2Context;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Interface_property_declarationContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Labeled_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Local_constant_declarationContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Lock_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Member_declaratorContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Member_nameContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Method_bodyContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Method_declaration2Context;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Method_declarationContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Method_headerContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Method_member_nameContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Namespace_bodyContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Namespace_declarationContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Namespace_member_declarationContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Namespace_nameContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Operator_bodyContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Operator_declaration2Context;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Operator_declarationContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Overloadable_binary_operatorContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Overloadable_operatorContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Overloadable_unary_operatorContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Parameter_arrayContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Property_declaration2Context;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Property_declarationContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Selection_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Simple_embedded_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Static_constructor_declarationContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Struct_bodyContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Struct_definitionContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Switch_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Throw_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Try_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Unary_operator_declaratorContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Unchecked_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Unsafe_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Using_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.While_statementContext;
import edu.montana.gsoc.msusel.parsers.csharp.CSharp6Parser.Yield_statementContext;

/**
 * Using the parser, this class incrementally builds a CodeTree one file at a
 * time.
 *
 * @author Isaac Griffith
 * @version 1.1.0
 */
public class CSharpCodeTreeBuilder extends CSharp6BaseListener {

    /**
     * Logger to log the process of the builder
     */
    private static final Logger               LOG = LoggerFactory.getLogger(CSharpCodeTreeBuilder.class);
    /**
     * Stack used for creating types
     */
    transient private final Stack<TypeNode>   types;
    /**
     * Stack used for creating methods
     */
    transient private final Stack<MethodNode> methods;
    /**
     * Stack used to buildup namespaces
     */
    transient private final Stack<String>     namespaces;
    /**
     * FileNode being built
     */
    private final FileNode                    file;
    /**
     * Line of Code Counter
     */
    private final LoCCounter                  locCounter;

    /**
     * Construct a new JavaCodeTreeBuilder for the provided FileNode
     * 
     * @param file
     */
    public CSharpCodeTreeBuilder(final FileNode file)
    {
        types = new Stack<>();
        methods = new Stack<>();
        namespaces = new Stack<>();
        this.file = file;
        locCounter = new LoCCounter("//", "/*", "*/", "\r\n");
    }

    /**
     * Adds the LOC metric measurement to the given INode based on the text from
     * the given ParserRuleContext
     * 
     * @param ctx
     *            ParserRuleContext
     * @param ent
     *            INode
     */
    private void addLoCMetric(final @NonNull ParserRuleContext ctx, final @NonNull INode ent)
    {
        locCounter.reset();
        locCounter.count(ctx.getText());
        ent.addMetric("LOC", new Double(locCounter.getSloc()));
    }

    /**
     * Creates an appropriate StatementNode for the given type and context.
     * 
     * @param type
     *            StatementType
     * @param ctx
     *            Context
     */
    private void createStatement(@NonNull StatementType type, @NonNull ParserRuleContext ctx)
    {
        if (!methods.isEmpty())
        {
            MethodNode mn = methods.peek();
            int start = ctx.getStart().getLine();
            int end = ctx.getStop().getLine();

            StatementNode sn = null;
            if (start > end)
            {
                sn = StatementNode.builder(type).range(start).create();
            }
            else
            {
                sn = StatementNode.builder(type).range(start, end).create();
            }
            mn.addStatement(sn);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterAccessor_declarations(Accessor_declarationsContext ctx)
    {
        // TODO Auto-generated method stub
        super.enterAccessor_declarations(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterAdd_accessor_declaration(Add_accessor_declarationContext ctx)
    {
        // TODO Auto-generated method stub
        super.enterAdd_accessor_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterBreak_statement(Break_statementContext ctx)
    {
        createStatement(StatementType.Break, ctx);
        super.enterBreak_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterChecked_statement(Checked_statementContext ctx)
    {
        createStatement(StatementType.Checked, ctx);
        super.enterChecked_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterClass_definition(final Class_definitionContext ctx)
    {
        final IdentifierContext itx = ctx.identifier();
        String name = itx.getText();
        if (name == null || name.isEmpty())
            name = "UNKNOWN";
        final String fullName = namespaces.isEmpty() ? name : namespaces.peek() + "." + name;
        int start = 1;
        if (ctx.getStart() != null)
            start = ctx.getStart().getLine();
        int end = start;
        if (ctx.getStop() != null)
            end = ctx.getStop().getLine();

        final TypeNode ent = TypeNode.builder(fullName == null ? name : fullName, name).range(start, end).create();
        file.addType(ent);
        types.push(ent);

        addLoCMetric(ctx, ent);

        super.enterClass_definition(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterCompilation_unit(Compilation_unitContext ctx)
    {
        super.enterCompilation_unit(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterConstructor_declaration2(final Constructor_declaration2Context ctx)
    {
        final IdentifierContext ictx = ctx.identifier();
        String name = ictx.getText();

        name += "(" + getParams(ctx.formal_parameter_list()) + ")";
        final String fullName = types.peek().getQIdentifier() + "#" + name;

        int start = 1;
        if (ctx.getStart() != null)
            start = ctx.getStart().getLine();
        int end = start;
        if (ctx.getStop() != null)
            end = ctx.getStop().getLine();

        final MethodNode ent = MethodNode.builder(fullName, name).constructor().range(start, end).create();
        types.peek().addMethod(ent);
        methods.push(ent);

        addLoCMetric(ctx, ent);

        super.enterConstructor_declaration2(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterContinue_statement(Continue_statementContext ctx)
    {
        createStatement(StatementType.Continue, ctx);
        super.enterContinue_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterDeclaration_statement(Declaration_statementContext ctx)
    {
        createStatement(StatementType.Declaration, ctx);
        super.enterDeclaration_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterDelegate_declaration(Delegate_declarationContext ctx)
    {
        final IdentifierContext mmctx = ctx.identifier();
        String name = mmctx == null ? "<DELEGATE>" : mmctx.getText();

        name += "(" + getParams(ctx.formal_parameter_list()) + ")";
        final String fullName = types.peek().getQIdentifier() + "#" + name;

        int start = ctx.getStart().getLine();
        int end = ctx.getStop().getLine();

        if (end < start)
            end = start;

        final MethodNode ent = MethodNode.builder(fullName, name).range(start, end).create();
        types.peek().addMethod(ent);
        methods.push(ent);

        addLoCMetric(ctx, ent);
        super.enterDelegate_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterDelegate_definition(final Delegate_definitionContext ctx)
    {
        // final String name = ctx.identifier() == null ? "" :
        // ctx.identifier().getText() ;
        // int start = 1; if (ctx.getStart() != null) =
        // ctx.getStart().getLine();
        // int end = start; if (ctx.getStop() != null) ctx.getStop().getLine();
        // final String fullName = namespaces.isEmpty() ? name :
        // namespaces.peek() + "." + name;
        // final TypeNode ent = new TypeNode(fullName == null ? name : fullName,
        // name, start, end);
        // file.addType(ent);
        // stack.push(ent);

        super.enterDelegate_definition(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterDelegate_type(Delegate_typeContext ctx)
    {
        // TODO Auto-generated method stub
        super.enterDelegate_type(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterDestructor_declaration(Destructor_declarationContext ctx)
    {
        String name = "<DESTRUCTOR>";

        name += "(" + ")";
        final String fullName = types.peek().getQIdentifier() + "#" + name;

        int start = ctx.getStart().getLine();
        int end = start;
        if (ctx.getStop() != null)
            end = ctx.getStop().getLine();

        if (end < start)
            end = start;

        final MethodNode ent = MethodNode.builder(fullName, name).range(start, end).create();
        types.peek().addMethod(ent);
        methods.push(ent);

        addLoCMetric(ctx, ent);
        super.enterDestructor_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterDo_statement(Do_statementContext ctx)
    {
        createStatement(StatementType.Do, ctx);
        super.enterDo_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterEmbedded_statement(Embedded_statementContext ctx)
    {
        createStatement(StatementType.Embedded, ctx);
        super.enterEmbedded_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterEmbedded_statement_unsafe(Embedded_statement_unsafeContext ctx)
    {
        createStatement(StatementType.UnsafeEmbedded, ctx);
        super.enterEmbedded_statement_unsafe(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterEmpty_statement(Empty_statementContext ctx)
    {
        createStatement(StatementType.Empty, ctx);
        super.enterEmpty_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterEnum_definition(final Enum_definitionContext ctx)
    {
        final String name = ctx.identifier() == null ? "" : ctx.identifier().getText();
        final String fullName = namespaces.isEmpty() ? name : namespaces.peek() + "." + name;
        int start = 1;
        if (ctx.getStart() != null)
            start = ctx.getStart().getLine();
        int end = start;
        if (ctx.getStop() != null)
            end = ctx.getStop().getLine();
        final TypeNode ent = TypeNode.builder(fullName, name).range(start, end).create();

        file.addType(ent);
        types.push(ent);

        addLoCMetric(ctx, ent);

        super.enterEnum_definition(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterEnum_member_declaration(Enum_member_declarationContext ctx)
    {
        String name = ctx.identifier() == null ? "" : ctx.identifier().getText();
        int start = ctx.getStart().getLine();
        int end = ctx.getStop().getLine();

        if (end < start)
            end = start;

        TypeNode type = types.peek();
        if (name != null)
        {
            type.addField(FieldNode.builder(name, type.getQIdentifier() + "#" + name).range(start, end).create());
        }
        super.enterEnum_member_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterExpression_statement(Expression_statementContext ctx)
    {
        createStatement(StatementType.Expression, ctx);
        super.enterExpression_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterFixed_statement(Fixed_statementContext ctx)
    {
        createStatement(StatementType.Fixed, ctx);
        super.enterFixed_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterFor_statement(For_statementContext ctx)
    {
        createStatement(StatementType.For, ctx);
        super.enterFor_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterForeach_statement(Foreach_statementContext ctx)
    {
        createStatement(StatementType.Foreach, ctx);
        super.enterForeach_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterGoto_statement(Goto_statementContext ctx)
    {
        createStatement(StatementType.Goto, ctx);
        super.enterGoto_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterIf_statement(If_statementContext ctx)
    {
        createStatement(StatementType.If, ctx);
        super.enterIf_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterIndexer_declaration(Indexer_declarationContext ctx)
    {
        // TODO Auto-generated method stub
        super.enterIndexer_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterIndexer_declaration2(Indexer_declaration2Context ctx)
    {
        // TODO Auto-generated method stub
        super.enterIndexer_declaration2(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterInterface_definition(final Interface_definitionContext ctx)
    {
        final String name = ctx.identifier() == null ? "" : ctx.identifier().getText();
        final String fullName = namespaces.isEmpty() ? name : namespaces.peek() + "." + name;
        int start = 1;
        if (ctx.getStart() != null)
            start = ctx.getStart().getLine();
        int end = start;
        if (ctx.getStop() != null)
            end = ctx.getStop().getLine();

        final TypeNode ent = TypeNode.builder(fullName, name).range(start, end).isInterface().create();

        types.push(ent);
        file.addType(ent);

        addLoCMetric(ctx, ent);

        super.enterInterface_definition(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitInterface_definition(final Interface_definitionContext ctx)
    {

        super.exitInterface_definition(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterInterface_method_declaration2(final Interface_method_declaration2Context ctx)
    {
        String name = ctx.identifier() == null ? "" : ctx.identifier().getText();
        name = name + " (" + getParams(ctx.formal_parameter_list()) + ")";
        final String fullName = types.peek().getQIdentifier() + "#" + name;
        int start = 1;
        if (ctx.getStart() != null)
            start = ctx.getStart().getLine();
        int end = start;
        if (ctx.getStop() != null)
            end = ctx.getStop().getLine();
        final MethodNode ent = MethodNode.builder(fullName, name).range(start, end).create();

        types.peek().addMethod(ent);

        super.enterInterface_method_declaration2(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterLabeled_statement(Labeled_statementContext ctx)
    {
        createStatement(StatementType.Labeled, ctx);
        super.enterLabeled_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterLock_statement(Lock_statementContext ctx)
    {
        createStatement(StatementType.Lock, ctx);
        super.enterLock_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterMember_declarator(Member_declaratorContext ctx)
    {
        // TODO Auto-generated method stub
        super.enterMember_declarator(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterMethod_declaration2(final Method_declaration2Context ctx)
    {
        final Method_member_nameContext mmctx = ctx.method_member_name();
        String name = mmctx == null ? "<METHOD>" : mmctx.getText();

        name += "(" + getParams(ctx.formal_parameter_list()) + ")";
        final String fullName = types.peek().getQIdentifier() + "#" + name;

        int start = ctx.getStart().getLine();
        int end = ctx.getStop().getLine();

        if (end < start)
            end = start;

        final MethodNode ent = MethodNode.builder(fullName, name).range(start, end).create();
        types.peek().addMethod(ent);
        methods.push(ent);

        addLoCMetric(ctx, ent);

        super.enterMethod_declaration2(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterNamespace_declaration(final Namespace_declarationContext ctx)
    {
        final List<IdentifierContext> ids = ctx.qualified_identifier().identifier();
        final StringBuilder builder = new StringBuilder();

        boolean first = true;
        for (final IdentifierContext idx : ids)
        {
            if (!first)
            {
                builder.append(".");
            }
            builder.append(idx.IDENTIFIER().getText());
            first = false;
        }
        String name = builder.toString();
        if (!namespaces.isEmpty())
        {
            name = namespaces.peek() + "." + name;
        }
        namespaces.push(name);

        super.enterNamespace_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterNamespace_member_declaration(Namespace_member_declarationContext ctx)
    {
        // TODO Auto-generated method stub
        super.enterNamespace_member_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterNamespace_name(Namespace_nameContext ctx)
    {
        // TODO Auto-generated method stub
        super.enterNamespace_name(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterOperator_declaration(Operator_declarationContext ctx)
    {
        String name = "<OPERATOR>";
        if (ctx.operator_declarator() != null)
        {
            if (ctx.operator_declarator().binary_operator_declarator() != null)
            {
                if (ctx.operator_declarator().binary_operator_declarator().overloadable_binary_operator() != null)
                {
                    Overloadable_binary_operatorContext oboc = ctx.operator_declarator()
                            .binary_operator_declarator()
                            .overloadable_binary_operator();
                    if (oboc.OP_EQ() != null)
                    {
                        name = oboc.OP_EQ().getText();
                    }
                    else if (oboc.OP_GE() != null)
                    {
                        name = oboc.OP_GE().getText();
                    }
                    else if (oboc.OP_LE() != null)
                    {
                        name = oboc.OP_LE().getText();
                    }
                    else if (oboc.OP_LEFT_SHIFT() != null)
                    {
                        name = oboc.OP_LEFT_SHIFT().getText();
                    }
                    else if (oboc.OP_NE() != null)
                    {
                        name = oboc.OP_NE().getText();
                    }
                    else if (oboc.AMP() != null)
                    {
                        name = oboc.AMP().getText();
                    }
                    else if (oboc.DIV() != null)
                    {
                        name = oboc.DIV().getText();
                    }
                    else if (oboc.BITWISE_OR() != null)
                    {
                        name = oboc.BITWISE_OR().getText();
                    }
                    else if (oboc.CARET() != null)
                    {
                        name = oboc.CARET().getText();
                    }
                    else if (oboc.GT() != null)
                    {
                        name = oboc.GT().getText();
                    }
                    else if (oboc.LT() != null)
                    {
                        name = oboc.LT().getText();
                    }
                    else if (oboc.MINUS() != null)
                    {
                        name = oboc.MINUS().getText();
                    }
                    else if (oboc.PERCENT() != null)
                    {
                        name = oboc.PERCENT().getText();
                    }
                    else if (oboc.PLUS() != null)
                    {
                        name = oboc.PLUS().getText();
                    }
                    else if (oboc.STAR() != null)
                    {
                        name = oboc.STAR().getText();
                    }
                    else if (oboc.right_shift() != null)
                    {
                        name = oboc.right_shift().getText();
                    }
                }
            }
            else if (ctx.operator_declarator().conversion_operator_declarator() != null)
            {
                if (ctx.operator_declarator().conversion_operator_declarator().identifier() != null)
                    name = ctx.operator_declarator().conversion_operator_declarator().identifier().getText();
                else if (ctx.operator_declarator().conversion_operator_declarator().OPERATOR() != null)
                    name = ctx.operator_declarator().conversion_operator_declarator().OPERATOR().getText();
            }
            else if (ctx.operator_declarator().unary_operator_declarator() != null)
            {
                if (ctx.operator_declarator().unary_operator_declarator().identifier() != null)
                {
                    name = ctx.operator_declarator().unary_operator_declarator().identifier().getText();
                }
                else if (ctx.operator_declarator().unary_operator_declarator().overloadable_unary_operator() != null)
                {
                    Overloadable_unary_operatorContext ouoc = ctx.operator_declarator()
                            .unary_operator_declarator()
                            .overloadable_unary_operator();
                    if (ouoc.OP_DEC() != null)
                    {
                        name = ouoc.OP_DEC().getText();
                    }
                    else if (ouoc.OP_INC() != null)
                    {
                        name = ouoc.OP_INC().getText();
                    }
                    else if (ouoc.BANG() != null)
                    {
                        name = ouoc.BANG().getText();
                    }
                    else if (ouoc.FALSE() != null)
                    {
                        name = ouoc.FALSE().getText();
                    }
                    else if (ouoc.MINUS() != null)
                    {
                        name = ouoc.MINUS().getText();
                    }
                    else if (ouoc.PLUS() != null)
                    {
                        name = ouoc.PLUS().getText();
                    }
                    else if (ouoc.TILDE() != null)
                    {
                        name = ouoc.TILDE().getText();
                    }
                    else if (ouoc.TRUE() != null)
                    {
                        name = ouoc.TRUE().getText();
                    }
                }
            }
        }
        final String op = name;
        int start = 1;
        if (ctx.getStart() != null)
            start = ctx.getStart().getLine();
        int end = start;
        if (ctx.getStop() != null)
            end = ctx.getStop().getLine();
        final String fullName = types.peek().getQIdentifier() + "#" + op;
        final MethodNode ent = MethodNode.builder(fullName, op).range(start, end).create();

        methods.push(ent);
        types.peek().addMethod(ent);

        addLoCMetric(ctx, ent);
        super.enterOperator_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterOperator_declaration2(final Operator_declaration2Context ctx)
    {
        String op = "<OPERATOR>";
        if (ctx.OPERATOR() != null)
        {
            op = ctx.OPERATOR().getText();
        }
        else if (ctx.overloadable_operator() != null)
        {
            op = ctx.overloadable_operator().getText();
        }

        int start = 1;
        if (ctx.getStart() != null)
            start = ctx.getStart().getLine();
        int end = start;
        if (ctx.getStop() != null)
            end = ctx.getStop().getLine();
        final String fullName = types.peek().getQIdentifier() + "#" + op;
        final MethodNode ent = MethodNode.builder(fullName, op).range(start, end).create();

        methods.push(ent);
        types.peek().addMethod(ent);

        addLoCMetric(ctx, ent);

        super.enterOperator_declaration2(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterOverloadable_binary_operator(Overloadable_binary_operatorContext ctx)
    {
        // TODO Auto-generated method stub
        super.enterOverloadable_binary_operator(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterOverloadable_operator(Overloadable_operatorContext ctx)
    {
        // TODO Auto-generated method stub
        super.enterOverloadable_operator(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterOverloadable_unary_operator(Overloadable_unary_operatorContext ctx)
    {
        // TODO Auto-generated method stub
        super.enterOverloadable_unary_operator(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterLocal_constant_declaration(Local_constant_declarationContext ctx)
    {
        // TODO Auto-generated method stub
        super.enterLocal_constant_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitMethod_declaration(Method_declarationContext ctx)
    {
        // TODO Auto-generated method stub
        super.exitMethod_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitConstructor_declaration(Constructor_declarationContext ctx)
    {
        // TODO Auto-generated method stub
        super.exitConstructor_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitConstructor_body(Constructor_bodyContext ctx)
    {
        super.exitConstructor_body(ctx);
        if (!methods.isEmpty())
            methods.pop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterStatic_constructor_declaration(Static_constructor_declarationContext ctx)
    {
        // TODO Auto-generated method stub
        super.enterStatic_constructor_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitDestructor_declaration(Destructor_declarationContext ctx)
    {
        // TODO Auto-generated method stub
        super.exitDestructor_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    public void exitDestructor_body(Destructor_bodyContext ctx)
    {
        super.exitDestructor_body(ctx);
        if (!methods.isEmpty())
            methods.pop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitInterface_method_declaration(Interface_method_declarationContext ctx)
    {
        // TODO Auto-generated method stub
        super.exitInterface_method_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterInterface_property_declaration(Interface_property_declarationContext ctx)
    {
        String name = ctx.identifier() == null ? "" : ctx.identifier().getText();
        int start = ctx.getStart().getLine();
        int end = ctx.getStop().getLine();

        if (end < start)
            end = start;

        TypeNode type = types.peek();
        if (name != null)
        {
            type.addField(FieldNode.builder(name, type.getQIdentifier() + "#" + name).range(start, end).create());
        }
        super.enterInterface_property_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitInterface_property_declaration(Interface_property_declarationContext ctx)
    {
        // TODO Auto-generated method stub
        super.exitInterface_property_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitDelegate_declaration(Delegate_declarationContext ctx)
    {
        super.exitDelegate_declaration(ctx);
        if (!methods.isEmpty())
            methods.pop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterField_declaration2(Field_declaration2Context ctx)
    {
        super.enterField_declaration2(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitConstructor_declaration2(Constructor_declaration2Context ctx)
    {
        // TODO Auto-generated method stub
        super.exitConstructor_declaration2(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitMethod_declaration2(Method_declaration2Context ctx)
    {
        // TODO Auto-generated method stub
        super.exitMethod_declaration2(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitInterface_method_declaration2(Interface_method_declaration2Context ctx)
    {
        // TODO Auto-generated method stub
        super.exitInterface_method_declaration2(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterInterface_property_declaration2(Interface_property_declaration2Context ctx)
    {
        String name = ctx.identifier() == null ? "" : ctx.identifier().getText();
        int start = ctx.getStart().getLine();
        int end = ctx.getStop().getLine();

        if (end < start)
            end = start;

        TypeNode type = types.peek();
        if (name != null)
        {
            type.addField(FieldNode.builder(name, type.getQIdentifier() + "#" + name).range(start, end).create());
        }
        super.enterInterface_property_declaration2(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitInterface_property_declaration2(Interface_property_declaration2Context ctx)
    {
        // TODO Auto-generated method stub
        super.exitInterface_property_declaration2(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterProperty_declaration(Property_declarationContext ctx)
    {
        String name = ctx.member_name().toString();
        int start = ctx.getStart().getLine();
        int end = ctx.getStop().getLine();

        if (end < start)
            end = start;

        TypeNode type = types.peek();
        if (name != null)
        {
            type.addField(FieldNode.builder(name, type.getQIdentifier() + "#" + name).range(start, end).create());
        }

        super.enterProperty_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterMethod_declaration(Method_declarationContext ctx)
    {
        final Method_headerContext mhcx = ctx.method_header();
        final Member_nameContext mmctx = mhcx.member_name();
        String name = mmctx == null ? "<METHOD>" : mmctx.getText();

        name += "(" + getParams(mhcx.formal_parameter_list()) + ")";
        final String fullName = types.peek().getQIdentifier() + "#" + name;

        int start = ctx.getStart().getLine();
        int end = ctx.getStop().getLine();

        if (end < start)
            end = start;

        final MethodNode ent = MethodNode.builder(fullName, name).range(start, end).create();
        types.peek().addMethod(ent);
        methods.push(ent);

        addLoCMetric(ctx, ent);

        super.enterMethod_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterEvent_declaration(Event_declarationContext ctx)
    {
        // TODO Auto-generated method stub
        super.enterEvent_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterIndexer_declarator(Indexer_declaratorContext ctx)
    {
        // TODO Auto-generated method stub
        super.enterIndexer_declarator(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterConstructor_declaration(Constructor_declarationContext ctx)
    {
        final Constructor_declaratorContext mhcx = ctx.constructor_declarator();
        String name = "<INIT>";

        name += "(" + getParams(mhcx.formal_parameter_list()) + ")";
        final String fullName = types.peek().getQIdentifier() + "#" + name;

        int start = ctx.getStart().getLine();
        int end = ctx.getStop().getLine();

        if (end < start)
            end = start;

        final MethodNode ent = MethodNode.builder(fullName, name).range(start, end).constructor().create();
        types.peek().addMethod(ent);
        methods.push(ent);

        addLoCMetric(ctx, ent);
        super.enterConstructor_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterInterface_method_declaration(Interface_method_declarationContext ctx)
    {
        String name = ctx.identifier() == null ? "<METHOD>"
                : ctx.identifier() == null ? "" : ctx.identifier().getText();

        name += "(" + getParams(ctx.formal_parameter_list()) + ")";
        final String fullName = types.peek().getQIdentifier() + "#" + name;

        int start = ctx.getStart().getLine();
        int end = ctx.getStop().getLine();

        if (end < start)
            end = start;

        final MethodNode ent = MethodNode.builder(fullName, name).range(start, end).create();
        types.peek().addMethod(ent);

        addLoCMetric(ctx, ent);
        super.enterInterface_method_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterInterface_event_declaration(Interface_event_declarationContext ctx)
    {
        final IdentifierContext ix = ctx.identifier();
        String name = ix == null ? "<METHOD>" : ix.getText();

        name += "(" + ")";
        final String fullName = types.peek().getQIdentifier() + "#" + name;

        int start = ctx.getStart().getLine();
        int end = start;
        if (ctx.getStop() == null)
            end = ctx.getStop().getLine();

        if (end < start)
            end = start;

        final MethodNode ent = MethodNode.builder(fullName, name).range(start, end).create();
        types.peek().addMethod(ent);

        addLoCMetric(ctx, ent);
        super.enterInterface_event_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterInterface_indexer_declaration(Interface_indexer_declarationContext ctx)
    {
        super.enterInterface_indexer_declaration(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterEvent_declaration2(Event_declaration2Context ctx)
    {

        super.enterEvent_declaration2(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterInterface_event_declaration2(Interface_event_declaration2Context ctx)
    {
        // TODO Auto-generated method stub
        super.enterInterface_event_declaration2(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterInterface_indexer_declaration2(Interface_indexer_declaration2Context ctx)
    {
        // TODO Auto-generated method stub
        super.enterInterface_indexer_declaration2(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterProperty_declaration2(Property_declaration2Context ctx)
    {
        String name = "<PROPERTY>";
        if (ctx.member_name() != null)
            name = ctx.member_name().getText();
        if (ctx.right_arrow() != null)
            name = ctx.right_arrow().first.getText();

        int start = ctx.getStart().getLine();

        int end = start;
        if (ctx.getStop() != null)
            end = ctx.getStop().getLine();

        if (end < start)
            end = start;

        TypeNode type = types.peek();
        if (name != null)
        {
            type.addField(FieldNode.builder(name, type.getQIdentifier() + "#" + name).range(start, end).create());
        }

        super.enterProperty_declaration2(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterSelection_statement(Selection_statementContext ctx)
    {
        createStatement(StatementType.Selection, ctx);
        super.enterSelection_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterSimple_embedded_statement(Simple_embedded_statementContext ctx)
    {
        createStatement(StatementType.SimpleEmbedded, ctx);
        super.enterSimple_embedded_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterStruct_definition(final Struct_definitionContext ctx)
    {
        final String name = ctx.identifier() == null ? "<STRUCT>" : ctx.identifier().getText();
        int start = 1;
        if (ctx.getStart() != null)
            start = ctx.getStart().getLine();
        int end = start;
        if (ctx.getStop() != null)
            end = ctx.getStop().getLine();

        final String fullName = namespaces.isEmpty() ? name : namespaces.peek() + "." + name;
        final TypeNode ent = TypeNode.builder(fullName, name).range(start, end).create();
        types.push(ent);
        file.addType(ent);

        addLoCMetric(ctx, ent);

        super.enterStruct_definition(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterSwitch_statement(Switch_statementContext ctx)
    {
        createStatement(StatementType.Switch, ctx);
        super.enterSwitch_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterThrow_statement(Throw_statementContext ctx)
    {
        createStatement(StatementType.Throw, ctx);
        super.enterThrow_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterTry_statement(Try_statementContext ctx)
    {
        createStatement(StatementType.Try, ctx);
        super.enterTry_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterUnary_operator_declarator(Unary_operator_declaratorContext ctx)
    {
        // TODO Auto-generated method stub
        super.enterUnary_operator_declarator(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterUnchecked_statement(Unchecked_statementContext ctx)
    {
        createStatement(StatementType.UnChecked, ctx);
        super.enterUnchecked_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterUnsafe_statement(Unsafe_statementContext ctx)
    {
        createStatement(StatementType.Unsafe, ctx);
        super.enterUnsafe_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterUsing_statement(Using_statementContext ctx)
    {
        createStatement(StatementType.Using, ctx);
        super.enterUsing_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterWhile_statement(While_statementContext ctx)
    {
        createStatement(StatementType.While, ctx);
        super.enterWhile_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enterYield_statement(Yield_statementContext ctx)
    {
        createStatement(StatementType.Yield, ctx);
        super.enterYield_statement(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitClass_body(final Class_bodyContext ctx)
    {
        super.exitClass_body(ctx);
        if (!types.isEmpty())
            types.pop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitDelegate_type(Delegate_typeContext ctx)
    {
        // TODO Auto-generated method stub
        super.exitDelegate_type(ctx);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitEnum_body(final Enum_bodyContext ctx)
    {
        super.exitEnum_body(ctx);
        if (!types.isEmpty())
            types.pop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitInterface_body(final Interface_bodyContext ctx)
    {
        super.exitInterface_body(ctx);
        if (!types.isEmpty())
            types.pop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitMethod_body(final Method_bodyContext ctx)
    {
        super.exitMethod_body(ctx);
        if (!methods.isEmpty())
            methods.pop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitNamespace_body(final Namespace_bodyContext ctx)
    {
        super.exitNamespace_body(ctx);
        if (!namespaces.isEmpty())
            namespaces.pop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitOperator_body(final Operator_bodyContext ctx)
    {
        super.exitOperator_body(ctx);
        if (!methods.isEmpty())
            methods.pop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void exitStruct_body(final Struct_bodyContext ctx)
    {
        super.exitStruct_body(ctx);
        if (!types.isEmpty())
            types.pop();
    }

    /**
     * Extracts a comma-delimited string of method parameters.
     * 
     * @param ctx
     *            Context containing the parameter list.
     * @return Comma-delimited String of method parameters.
     */
    private String getParams(final Formal_parameter_listContext ctx)
    {
        String retVal = "";
        if (ctx != null)
        {
            final Fixed_parametersContext fpc = ctx.fixed_parameters();
            if (fpc != null)
            {
                final StringBuilder builder = new StringBuilder();
                String type = null;
                for (final Fixed_parameterContext pc : fpc.fixed_parameter())
                {
                    type = pc.type().getText();
                    builder.append(type + ", ");
                }
                retVal = builder.toString();
            }
            final Parameter_arrayContext pac = ctx.parameter_array();
            if (pac != null)
            {
                String type = "";
                if (pac.array_type() != null)
                {
                    type = pac.array_type().getText();
                }
                retVal += type;
            }
            if (retVal.endsWith(", "))
            {
                retVal = retVal.substring(0, retVal.length() - 2);
            }
        }
        return retVal;
    }
}
