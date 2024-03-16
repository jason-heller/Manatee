package lwjgui.loader;

public class TagLoader
{
	static final Tag ROOT_TAG = new Tag("html", TagData.NO_OPERATION);

	public static Tag parse(String content, Styles styles, String filename)
	{
		Tag root = ROOT_TAG;

		final int len = content.length();
		int line = 1;

		StringBuilder read = new StringBuilder();
		String currentAttrib = null;

		boolean inBrackets = false;
		boolean inQuotes = false;
		boolean inStyles = false;

		boolean closingTag = false;

		Tag currentTag = null;
		Tag parent = root;

		for (int pos = 0; pos < len; pos++)
		{
			char c = content.charAt(pos);

			if (inStyles)
			{
				read.append(c);

				if (c == '<')
				{
					inStyles = false;
					styles.addStyles(StyleLoader.parse(read.toString()));
					read.setLength(0);

					currentTag = new Tag(parent);
					inBrackets = true;
				}
			} else if (inQuotes)
			{
				if (c == '\"' || c == '\'')
				{
					inQuotes = false;

					if (currentAttrib != null)
					{
						currentTag.getAttributes().put(currentAttrib.toLowerCase(), read.toString());
						currentAttrib = null;
					} else
					{
						throw new TagParseException("Misplaced quote in file " + filename, line, pos);
					}

					read.setLength(0);
				} else
				{
					read.append(c);
				}
			} else if (inBrackets)
			{
				switch (c)
				{

				case ' ':
				{
					if (currentTag.getData() == null)
						setTagData(currentTag, read);

					else if (currentAttrib != null && read.length() > 0)
					{
						currentTag.getAttributes().put(currentAttrib.toLowerCase(), read.toString());
						currentAttrib = null;
					}

					read.setLength(0);
					break;
				}
				case '=':
				{
					if (currentAttrib == null)
					{
						currentAttrib = read.toString();
					}
					
					read.setLength(0);
					break;
				}
				case '\"':
				case '\'':
				{
					inQuotes = true;
					read.setLength(0);
					break;
				}
				case '/':
				{
					closingTag = true;
					break;
				}
				case '<':
				{
					throw new TagParseException("Misplaced opening bracket in file " + filename, line, pos);
				}
				case '>':
				{
					// Ignore comments
					if (read.toString().equals("--"))
					{
						inBrackets = false;
						closingTag = false;
						read.setLength(0);
						break;
					}
					
					// Commit tag
					if (currentTag.getData() == null)
						setTagData(currentTag, read);
					
					if (currentAttrib != null && read.length() > 0)
					{
						currentTag.getAttributes().put(currentAttrib.toLowerCase(), read.toString());
						currentAttrib = null;
					}

					if (closingTag)
					{
						if (parent == null)
						{
							throw new TagParseException("Tag hierarchy failed, no parent. " + filename, line, pos);
						}

						// Allows for shorthand of Tags such as <input />
						if (!parent.getName().equals(currentTag.getName()))
						{
							parent.getChildren().add(currentTag);
						} else
						{
							parent = parent.getParent();
						}

						currentTag = null;
					} else
					{
						parent.getChildren().add(currentTag);

						if (!currentTag.getData().isSelfClosing())
						{
							parent = currentTag;
						}

						if (currentTag.getData() == TagData.STYLE)
							inStyles = true;
					}

					inBrackets = false;
					closingTag = false;
					read.setLength(0);
					break;
				}
				default:
				{
					read.append(c);
				}
				}
			} else
			{
				switch (c)
				{
				case '<':
				{
					currentTag = new Tag(parent);
					inBrackets = true;
					break;
				}
				case '>':
				{
					throw new TagParseException("Misplaced closing bracket in file " + filename, line, pos);
				}
				case '\n':
				{
					line++;
					break;
				}
				}
			}
		}

		if (parent != root)
			System.err.println("Warning: unclosed tag in file " + filename);

		// root.print();

		return root;
	}

	private static void setTagData(Tag tag, StringBuilder read)
	{
		final String name = read.toString();
		final String comp = name.toUpperCase();

		tag.setName(name);

		for (TagData data : TagData.values())
		{
			if (data.name().equals(comp))
			{
				tag.setData(data);
				return;
			}
		}

		tag.setData(TagData.NO_OPERATION);
	}
}