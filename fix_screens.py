import re

def fix_screen(filepath, bar_color="MaterialTheme.colorScheme.primary"):
    with open(filepath, 'r', encoding='utf-8') as f:
        content = f.read()

    # 1. Update Card declaration
    old_card = '''                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(12.dp),
                        colors = CardDefaults.cardColors(containerColor = Color(0xFF232D42)),
                        border = BorderStroke(0.5.dp, Color(0xFF37474F))
                    ) {
                        Column(modifier = Modifier.padding(16.dp).fillMaxWidth()) {'''
    new_card = f'''                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        shape = RoundedCornerShape(16.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                        border = BorderStroke(0.5.dp, MaterialTheme.colorScheme.outlineVariant)
                    ) {{
                        Row(modifier = Modifier.fillMaxWidth().height(IntrinsicSize.Min)) {{
                            Box(
                                modifier = Modifier
                                    .fillMaxHeight()
                                    .width(8.dp)
                                    .background({bar_color})
                            )
                            Column(modifier = Modifier.padding(16.dp).weight(1f)) {{'''
    content = content.replace(old_card, new_card)

    # 2. Close the Row at the end of the Card.
    # The end of the card is usually:
    #                             }
    #                         }
    #                     }
    #                 }
    #             }
    #         }
    # 
    #         if (showDialog) {
    old_end = '''                            }
                        }
                    }
                }
            }
        }

        if (showDialog) {'''
    new_end = '''                            }
                        }
                        }
                    }
                }
            }
        }

        if (showDialog) {'''
    content = content.replace(old_end, new_end)
    
    # Also another variant with different spacing might exist
    old_end2 = '''                            }
                        }
                    }
                }
            }
        }
        
        if (showDialog) {'''
    new_end2 = '''                            }
                        }
                        }
                    }
                }
            }
        }
        
        if (showDialog) {'''
    content = content.replace(old_end2, new_end2)

    # 3. Replace text colors
    content = content.replace('color = Color.White', 'color = MaterialTheme.colorScheme.onSurface')
    content = content.replace('color = Color(0xFF90A4AE)', 'color = MaterialTheme.colorScheme.onSurfaceVariant')
    content = content.replace('tint = Color(0xFF90A4AE)', 'tint = MaterialTheme.colorScheme.onSurfaceVariant')
    content = content.replace('Color(0xFF90A4AE)', 'MaterialTheme.colorScheme.onSurfaceVariant') # for backgrounds like in QuartelScreen
    
    # specific colors for modals? Wait, there are modal bottoms in these files too.
    # The bottom sheets have: 
    # ModalBottomSheet( ... )
    # Let's fix their container colors too just in case, if they have any.
    # Looks like they don't have hardcoded container colors, they use default.

    with open(filepath, 'w', encoding='utf-8') as f:
        f.write(content)

fix_screen('c:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/app/src/main/java/com/andrefdias/dailynote/ui/screens/militar/MilitarScreen.kt', 'MaterialTheme.colorScheme.secondary')
fix_screen('c:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/app/src/main/java/com/andrefdias/dailynote/ui/screens/viatura/ViaturaScreen.kt', 'MaterialTheme.colorScheme.tertiary')
fix_screen('c:/Users/andre_we17otv/AndroidStudioProjects/DailyNotes/app/src/main/java/com/andrefdias/dailynote/ui/screens/quartel/QuartelScreen.kt', 'MaterialTheme.colorScheme.primary')
